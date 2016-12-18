TwitterBot {
	var launch_sclang;
	var tweepy;
	var <tweet, <makeTweet;
	var <entriesPerDir;
	var <scHelpDirs;
	var <getPath;
	var <helpDirs;
	var <numOfHelpFiles;
	var <>scHelpFiles;
	var <logFile;
	var <doc, <docPath, <docTitle, <docSummary, <docLink;
	var <playableExamples;
	var <helpFile;
	var <keysPath;
	var <rndSeed = 1230;
	var <routine;

    *new { //| path |
        ^super.new.init;//(path)
    }

    init {
		this.currPath;
		this.getHelpFiles;
		this.readLog;
		this.writeLog;
		this.composeTweet;
		this.initTweepy;
		this.runBot;
    }

	currPath {
		getPath = File.getcwd;
	}

	getHelpFiles {
		var regex, fn, p;
		var dirPath, absPath;

		scHelpFiles = IdentityDictionary.new;
		keysPath = IdentityDictionary.new;
		p = PathName(SCDoc.helpSourceDir);

		p.filesDo { | file |
			dirPath = file.pathOnly;
			absPath = file.fullPath;
			regex = file.fileName.findRegexp("^([^.]*).*(?<=\.)(\.schelp)");

			if(regex.size == 3){
				fn = regex[1][1] ++ regex[2][1];
				// create unique entries in IdentityDictionary
				scHelpFiles.put(fn.split($.)[0].asSymbol, false);
				keysPath.put(fn.split($.)[0].asSymbol, (absPath: absPath, dirPath: dirPath));
			};
		};

	}

	readLog {
		var fn, newLine, helpKeys;

		fn = File(this.getPath ++ "/bot-data/tweets-history.log", "a+");
		logFile = fn.readAllString;
		fn.close;
		newLine = logFile.findRegexp("\n"); // find new lines
		helpKeys = Array.newClear(newLine.size);

		helpKeys.size do: { |i|
			if(i == 0){
				helpKeys[i] = logFile.copyRange(0, newLine[i][0]-1)
			}{
				helpKeys[i] = logFile.copyRange(newLine[i-1][0]+1, newLine[i][0]-1)
			}
		};

		if(scHelpFiles.keys.size > 1){
			helpKeys do: { |i|
				var currKey = i.split($.)[0].asSymbol;
				if(scHelpFiles.includesKey(currKey)){
					scHelpFiles[currKey] = true
				}
			};
		};
	}

	writeLog {
		var	randomClass, fn, r, cnt = 0, rnd = IdentitySet[];
		var filePath = this.getPath ++ "/bot-data/tweets-history.log";

		fn = File(filePath, "a+");
		scHelpFiles.keysDo { |key| if(scHelpFiles[key]==true){ cnt=cnt+1 }};
		"LOG-SIZE = ".post; cnt.postln;

		if(cnt < scHelpFiles.keys.size){
			// choose file
			scHelpFiles.keysDo { |key|
				if(scHelpFiles[key] == false){
					rnd.add(key);
				};
			};

			randomClass = rnd.choose;
			scHelpFiles[randomClass] = true;
			"CHOOSEN: ".post; randomClass.postln;

			fn.write(randomClass.asString ++ "\n");
			fn.close;

			this.readSCDoc(randomClass)
		}{
			fn.close;
			scHelpFiles.keysDo { |key| scHelpFiles[key]==false };
			fn = File(filePath, "w");
			fn.write("");
			fn.close;
			this.init
		}
	}

	readSCDoc { | file |
		var checkUGen, tmp;

		docPath = keysPath[file]['absPath'];
		doc = SCDoc.parseFileFull(docPath);

		try {
			docTitle = doc.findChild(\HEADER).findChild(\TITLE).text;
			docSummary = doc.findChild(\HEADER).findChild(\SUMMARY).text;

			tmp = docPath.asString.replace(SCDoc.helpSourceDir, "");
			tmp = tmp.replace(".schelp", "");
			docLink = "doc.sccode.org" ++ tmp ++ ".html";

			checkUGen = doc.findChild(\HEADER).findChild(\CATEGORIES).findChild(\STRING);

			// if(checkUGen.asString.contains("UGens")){
			// 	this.readUGen
			// }
		}{
			| error |
			error.postln; \cleanup.postln;
		};
		\continued.postln;
	}

	readUGen {
		var example, regex, server, mygen, arr, maxIdx, eventCond;

		example = doc.findChild(\BODY).findChild(\EXAMPLES).findChild(\CODEBLOCK);
		regex = example.asString.findRegexp("\{.*?\}\.play(.*?);?");

		"REGEX = ".post; regex.postln;

		playableExamples = Array.newClear(regex.size);

		regex.size do: { | i |
			if((regex[i][1].size < 132) && (regex[i][1] != "")){
				playableExamples[i] = regex[i][1].asString;
			}
		};

		"EXAMPLES = ".post; playableExamples.postln;
		playableExamples do: {|item, i| format("%. %", i, item).postln;};
		arr = Array.newClear(playableExamples.size);
		playableExamples do: { |item, i|
			arr[i] = item.asString.size
		};
		maxIdx = arr.maxIndex;

		"PLAYABLE ====== ".post; playableExamples[maxIdx].postln;

		if ((playableExamples[maxIdx].notNil) && (playableExamples[maxIdx].size < 132)){
			"=-------------OUTSIDE------------=".postln;
			try {
				"=-------------INSIDE 1------------=".postln;

				{
					"=-------------INSIDE 2------------=".postln;
					server = Server.default;
					server.waitForBoot {
						protect {
							mygen = playableExamples[maxIdx].interpret;
						}{
							|error|
							eventCond = (catchServer: error);
							"::::::::::::::::::::::::::::::".postln;
							"PROTECTED ERROR:::::::::::::::".post; error.postln;
							"::::::::::::::::::::::::::::::".postln;
						}
					};
					4.wait;
					mygen.free;
					server.quit;
				}.fork;
				\tweetTried.postln;
			}{
				| error |
				error.postln; \cleanup.postln;
			};

			if(eventCond.catchServer.isNil){
				makeTweet = playableExamples[maxIdx];
				\successAndContinue.postln;
			}{
				makeTweet = nil;
				\serverFailedToRunTweet.postln;
			}
		}
	}

	composeTweet {
		tweet = format("% : %\n %", docTitle, docSummary, docLink);
		"TWEET =====>>>>>> ".post; tweet.postln;
		"MAKE-TWEET =========>>>>>".post; makeTweet.postln;
	}


	initTweepy {
		var myPath = this.getPath;

		tweepy = format("cd " ++ myPath ++ " &&
python -c %import tweepy
from keys import keys

CONSUMER_KEY = keys['consumer_key']
CONSUMER_SECRET = keys['consumer_secret']
ACCESS_TOKEN = keys['access_token']
ACCESS_TOKEN_SECRET = keys['access_token_secret']

auth = tweepy.OAuthHandler(CONSUMER_KEY, CONSUMER_SECRET)
auth.set_access_token(ACCESS_TOKEN, ACCESS_TOKEN_SECRET)

api = tweepy.API(auth)
user = api.get_user('sc3_bot')

print(user.followers_count)
for friend in user.friends():
   print (friend.screen_name)%
", '"'.asString, '"'.asString);

	}

	runBot {
		tweepy.unixCmd;
	}

}


/*
	d = SCDoc.new;
	d.parseFileMetaData(SCDoc.helpSourceDir)
	Post<<*
	d = SCDoc.parseFileFull("/usr/share/SuperCollider/HelpSource/Help.schelp");

	d.class
	d.id
	d.text
	d.children
	d.findChild(\HEADER).findChild(\TITLE).text
	d.findChild(\HEADER).findChild(\SUMMARY).text
	d.findChild(\HEADER).children

	//
	d = SCDoc.parseFileFull("/usr/share/SuperCollider/HelpSource/Classes/SinOsc.schelp");
	d.findChild(\HEADER).children
	Post<<*d.children
	d.children[0]
	d.children[1]

	Post<<*d.findChild(\BODY).children

	d.findChild(\BODY).findChild(\EXAMPLES)

*/
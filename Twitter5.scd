TwitterBot {
	var tweepy, tweet;
	var <getPath;
	var <numOfHelpFiles;
	var <scHelpFiles, <keysPath;
	var <logFile;
	var <doc, <docPath, <docTitle, <docSummary, <docLink;
	var <playableExamples;
	var <ugenCond;
	var <myugen;

    *new {
        ^super.new.init;
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
		numOfHelpFiles = scHelpFiles.keys.size;
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
		var randomClass, fn, r, cnt = 0, rnd = IdentitySet[];
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
			"CLASS SELECTED: ".post; randomClass.postln;

			fn.write(randomClass.asString ++ "\n");
			fn.close;

			this.readSCDoc(randomClass)
		}{ // clean log file and start again
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

			if(checkUGen.asString.contains("UGens")){
				this.readUGen(file)
			}
		}{
			| error |
			error.postln; \cleanup.postln;
		};
		\continued.postln;
	}

	readUGen { | file |
		var f, example, regex, arr, maxidx, eventCond;
		var syntaxError = false;
		var ugenName = false;
		var myUGenClass;

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

		maxidx = arr.maxIndex;
		myugen = playableExamples[maxidx];

		myugen.find(" ") do: { |i|
			myugen = myugen.replace(" ", "");
		};

		myugen.find("\t") do: { |i|
			myugen = myugen.replace("\t", "");
		};

		myugen.find("\n") do: { |i|
			myugen = myugen.replace("\n", "");
		};

		myugen.find("=-") do: { |i|
			myugen = myugen.replace("=-", "= -");
		};

		myugen.find("* -") do: { |i|
			myugen = myugen.replace("*-", "* -");
		};

		myugen = myugen.replace("var", "var ");
		myugen = myugen.replace("arg", "arg ");

		"PLAYABLE ====== ".post; myugen.postln;

		// check for syntax errors
		myUGenClass = myugen.compile.class;

		if(myUGenClass == Function){
			syntaxError = false;
		}{
			syntaxError = true;
		};

		// check if class name is in the ugen
		ugenName = ("(?<![A-Za-z0-9"++file.asString++"])("++file.asString++"[\.akir\(]+)").matchRegexp(myugen);

		format("FILE-AS-ARG-CLASS: %", file.class).postln;
		format("FILE-AS-ARG-CLASS-AS-STRING: %", file.asString.class).postln;
		format("UGENNAME: %", ugenName).postln;

		"OUTSIDE - IF".postln;
		"syntaxError: ".post; syntaxError.postln;

		if((myugen.notNil) && (syntaxError == false) && (ugenName == true)){
			//if((myugen.notNil) && (ugenName == true)){
			"INSIDE - IF".postln;
			this.playUGen(myugen)
		}
	}

	playUGen { | myugen |
		var server;

		ugenCond = true;
		"----------INFORK------------".postln;

		try{
			fork{
				protect{
					server = Server.local;
					server.waitForBoot{
						try {
							myugen.interpret;
						}{ |err|
							[\onWaitForBoot, err].postln;
							if(err.notNil){
								ugenCond = false;
							}
						}
					};
					4.wait;
					server.quit;
				}{|err|
					[\onProtect, err].postln;
				}
			};
			//"UGENCOND: ".post; ugenCond.postln;
		}{ |err| [\onFork, err].postln; };
		"UGENCOND: ".post; ugenCond.postln;
	}

	composeTweet {
		tweet = format("%: %\n %", docTitle, docSummary, docLink);
		"TWEET: ".post; tweet.postln;
	}

	initTweepy {
		var myPath = this.getPath;
		var postUGen;

		if(ugenCond == true){
			postUGen = True;
		};

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

# tweet help file
api.update_status(status=%%%)

# post UGen tweet
try:
    if % == True:
        api.update_status(status=%%%)
except:
    pass

followers = api.followers_ids()
friends = api.friends_ids()

# follow back followers
for follower in tweepy.Cursor(api.followers).items():
    try:
        follower.follow()
    except:
        pass

# unfollow non-followers
for i in friends:
    if i not in followers:
        try:
            api.destroy_friendship(i)
        except:
            pass%
", '"'.asString, "'", tweet, "'", postUGen.value, "'", myugen, "'", '"'.asString);

	}

	runBot {
		//tweepy.unixCmd;
		tweepy.postln
	}
}
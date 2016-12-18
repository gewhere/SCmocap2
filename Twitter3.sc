TwitterBot {
	var launch_sclang;
	var tweepy;
	var <entriesPerDir;
	var <scHelpDirs;
	var <getPath;
	var <helpDirs;
	var <numOfHelpFiles;
	var <>scHelpFiles;
	var <logFile;
	var <doc, <docPath, <docTitle, <docSummary;
	var <playableExamples;
	
    *new { //| path |
        ^super.new.initMe;//(path)
    }

    initMe {
		this.currPath;
		this.helpFileDirs;
		this.countHelpFiles;
		this.totalHelpFiles;
		this.identityHelpFiles;
		this.readLog;
		this.writeLog;
		//this.getWeights;
		//this.initTweepy;
		//this.runBot;
    }
	
	currPath {
		getPath = "pwd".unixCmdGetStdOut;
		getPath = getPath.copyRange(0, getPath.size-2); //remove \n
	}

	helpFileDirs {
		var dirPaths, newLine;
		// $NF get the last column of awk
		dirPaths = ("find " ++ SCDoc.helpSourceDir ++ " -type d -ls | awk '{print $NF}'").unixCmdGetStdOut;
		newLine = dirPaths.findRegexp("\n"); // find new lines
		helpDirs = Array.newClear(newLine.size);

		helpDirs.size do: { |i|
			if(i == 0){
				helpDirs[i] = dirPaths.copyRange(0, newLine[i][0]-1)
			}{
				helpDirs[i] = dirPaths.copyRange(newLine[i-1][0]+1, newLine[i][0]-1)
			}
		};
	}

	countHelpFiles {
		var howManyEntries, stdOut;
		// how many schelp files in each directory
		entriesPerDir = IdentityDictionary.new;
		
		this.helpDirs do: { |i|
			howManyEntries = "cd " ++ i ++ " 2>/dev/null && ls *.schelp 2>/dev/null | wc -l";
			stdOut = howManyEntries.unixCmdGetStdOut;
			entriesPerDir.put(i.asSymbol, stdOut.copyRange(0, stdOut.size-2));
		};
		
		this.selectHelpDirs(entriesPerDir)
	}

	selectHelpDirs { | dict |
		scHelpDirs = dict;

		dict.keysDo{ | key |
			if((dict[key] == "0")||(dict[key] == "")){
				scHelpDirs.removeAt(key);
			};
		};
	}

	totalHelpFiles {
		// convert values to Integers
		scHelpDirs.keysDo { | key, i |
			scHelpDirs[key] = scHelpDirs.values[i].asInteger;
		};
		
		numOfHelpFiles = scHelpDirs.values.sum;
	}

	identityHelpFiles {
		var path, fileBase, fileExtension, fileName;
		scHelpFiles = IdentityDictionary.new;
		//^([^.]*).*(?<=\.)(.*)
		scHelpDirs.keys do: { | key |
			var myPath, tmp;

			myPath = PathName(key.asString);
			myPath.files do: { | file |
				if(file.extension == "schelp"){
					tmp = file.fileName.findRegexp("^([^.]*).*(?<=\.)(\.schelp)");
					fileBase = tmp[1][1];
					fileExtension = tmp[2][1];
					fileName = fileBase ++ fileExtension;
					path = PathName(key.asString ++ "/" ++ fileName);

					if(path.isFile){
						scHelpFiles.put(fileBase.asSymbol, key);
					}
				}
			}
		}
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
					scHelpFiles.removeAt(currKey)
				}
			};
		};
	}

	writeLog {
		var	randomClass, fn;

		fn = File(this.getPath ++ "/bot-data/tweets-history.log", "a+");
		
		"SCHELPFILES-SIZE = ".post; scHelpFiles.keys.size.postln;
		thisThread.randSeed = 1923;
		randomClass = scHelpFiles.keys.choose;
		//fn.postln;
		if((randomClass.asString++"\.schelp").matchRegexp(logFile)){
			// if match choose a new class
			"RANDOM CHOICE: ".post; randomClass.postln;
			fn.close;
			scHelpFiles.removeAt(randomClass);
			this.writeLog
		}{
			fn.write(randomClass.asString ++ ".schelp\n");
			fn.close;
			//fn.postln;
			"CHOOSEN >>>>> ".post; randomClass.postln;
			this.readSCDoc(randomClass)
		};
	}

	readSCDoc { | file |
		var checkUGen;
		
		docPath = scHelpFiles[file] ++ "/" ++ file ++ ".schelp";
		doc = SCDoc.parseFileFull(docPath);

		try {
			docTitle = doc.findChild(\HEADER).findChild(\TITLE).text;
			docSummary = doc.findChild(\HEADER).findChild(\SUMMARY).text;
			checkUGen = doc.findChild(\HEADER).findChild(\CATEGORIES).findChild(\STRING);

			if(checkUGen.asString.contains("UGens")){
				this.readUGen
			}
		}{
			| error |
			error.postln; \cleanup.postln;
		};
		\continued.postln;
	}

	readUGen {
		var example, regex;

		try {
			example = doc.findChild(\BODY).findChild(\EXAMPLES).findChild(\CODEBLOCK);
			regex = example.asString.findRegexp("\{.*?\}\.play(.*?);?");

			"REGEX = ".post; regex.postln;

			playableExamples = Array.newClear(regex.size);
			
			regex.size do: { | i |
				playableExamples[i] = regex[i][1];
			};
			
			"EXAMPLES = ".post; playableExamples.postln;

		}{
			| error |
			error.postln; \cleanup.postln;
		};
		\continued.postln;
	}

	getWeights {
		
	}

	
	
	initTweepy {
		var myPath = this.getPath.withoutTrailingSlash;//escapeChar($\n);

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
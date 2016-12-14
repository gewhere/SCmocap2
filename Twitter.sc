TwitterBot {
	var launch_sclang;
	var tweepy;
	var <entriesPerDir;
	var <scHelpDirs;
	var <getPath;
	var <helpDirs;
	var <numOfHelpFiles;
	var <scHelpFiles;
	
    *new { //| path |
        ^super.new.initMe;//(path)
    }

    initMe {
		this.currPath;
		this.helpFileDirs;
		this.countHelpFiles;
		this.totalHelpFiles;
		this.chooseHelpFile;
		//this.getWeights;
		//this.initTweepy;
		//this.runBot;
    }
	
	currPath {
		getPath = "pwd".unixCmdGetStdOut; 
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

	chooseHelpFile {
		scHelpFiles = IdentityDictionary.new;
		
		scHelpDirs.keysDo { | key |
			var myPath;
			var listOfFiles = IdentitySet.new;
			myPath = PathName.new(key.asString);
			myPath.filesDo { | file |
				//file.postln;
				if(file.extension == "schelp"){
					scHelpFiles.put(key,listOfFiles.add(file.fileNameWithoutExtension));
					//file.fileName.postln;
				}
			}
		}
	}

	rwLog {
		
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
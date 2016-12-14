MCread {

	var <>fn, <>data=nil;
	var <>param;

	*new { | filename |
		^super.new.init(filename)
	}

	init { | filename |
		this.readFile(filename)

	}

	readFile { | filename |
		// check if file exists
		fn = TabFileReader.read(filename);
		this.loadParameters(filename);

	}

	loadParameters { | filename |
		var allMarkersNames, tmp;

		param = IdentityDictionary.new;
		// remove the 1st element from markername ("MARKER_NAMES")
		tmp = fn[18].size - 1;
		allMarkersNames = Array.newClear(tmp);
		allMarkersNames.size do: { |i|
			allMarkersNames[i] = fn[18][i+1];
		};

		param.putPairs([
			datatype: "MoCap data",
			filename: filename,
			nFrames: fn[0][1],
			nCameras: fn[2][1],
			nMarkers: fn[4][1],
			frequency: fn[6][1],
			nAnalog: fn[8][1],
			timederOrder: 0,
			markerName: allMarkersNames,
			data: (),
			analogdata: fn[10][1],
			other: fn[14][1] // TIME_STAMP
		]);
		this.cleanData;

		// parameters.put(\data, data);
	}

	cleanData {
		// this method removes the first lines which have the metadata like date-time, total markers, etc. (and any empty lines produced from Stream -- see. FileReader)
		// Also fixes the data that I get from TabFileReader, for some reason I cannot do binary operations on the array if I do not clean them first (as below -- I am not sure how 'hacky' is this, I think as far it works is fine! Occam's razor)
		var str, cleanStr, array;

		fn.size do: { |i|
			if( (i > 18) && (i%2 == 0) ){
				str = fn[i].cs;
				cleanStr = str.findRegexp("\-?[0-9]+\.[0-9]+");

				array = Array.newClear(186);
				cleanStr.size.do { | i |
					array[i] = cleanStr[i][1].interpret;
				};
				// parameters['data'] is an event. Its indexes are even numbers starting from zero (0)
				this.param['data'].put((i - 20).asSymbol, array.clump(3));
			};
		};
	}
}
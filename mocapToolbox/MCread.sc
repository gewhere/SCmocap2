MCread {

	var <>fn, <>data=nil;
	var <>param, <markersNum;

	*new { | filename |
		^super.new.init(filename)
	}

	init { | filename |
		this.readFile(filename)

	}

	readFile { | filename |
		var data;
		// check if file exists
		data = TabFileReader.read(filename);
		data = data select: { |v| v[0].isEmpty.not };
		//fn.collect { |i,idx| if(i[0].isEmpty){ fn.removeAt(idx); } };
		fn = data;
		this.loadParameters(filename);
	}

	loadParameters { | filename |
		var allMarkersNames, tmp;

		param = IdentityDictionary.new;
		// remove the 1st element from markername ("MARKER_NAMES")
		tmp = fn[9].size - 1;
		allMarkersNames = Array.newClear(tmp);
		allMarkersNames.size do: { |i|
			allMarkersNames[i] = fn[9][i+1];
		};

		param.putPairs([
			datatype: "MoCap data",
			filename: filename,
			nFrames: fn[0][1].interpret,
			nCameras: fn[1][1].interpret,
			nMarkers: fn[2][1].interpret,
			frequency: fn[3][1].interpret,
			nAnalog: fn[4][1].interpret,
			timederOrder: 0,
			markerName: allMarkersNames,
			data: IdentityDictionary(),//Array2D
			analogdata: fn[5][1].interpret,
			other: fn[7][1] // TIME_STAMP
		]);

		markersNum = param['nMarkers'];

		this.cleanData;
	}

	cleanData {
		// this method removes the first lines which have the metadata like date-time, total markers, etc. (and any empty lines produced from Stream -- see. FileReader)
		var str, cleanStr, array, num;

		num = markersNum * 3;

		fn.size do: { |i|
			if( i > 9 ){
				str = fn[i].cs;
				cleanStr = str.findRegexp("\-?[0-9]+\.[0-9]+");

				array = Array.newClear(num);

				cleanStr.size.do { | i |
					array[i] = cleanStr[i][1].interpret;
				};
				// put(i-9) to ensure that enumeration of the 1st frame starts from 1
				this.param['data'].put(i-9, array.clump(3));
			};
		};
	}
}
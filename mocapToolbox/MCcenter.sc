MCcenter {

	var <param;

	*new { | dataStruct |
		^super.new.init(dataStruct)
	}

	init { | dataStruct |
		this.getCenter(dataStruct)
	}

	getCenter { | dataStruct |

		param = IdentityDictionary.new;

		param.putPairs([
			datatype: "MoCap data",
			filename: dataStruct.param['filename'],
			nFrames: dataStruct.param['nFrames'],
			nCameras: dataStruct.param['nCameras'],
			nMarkers: dataStruct.param['nMarkers'],
			frequency: dataStruct.param['frequency'],
			nAnalog: dataStruct.param['nAnalog'],
			timederOrder: 0,
			markerName: dataStruct.param['markerName'],
			data: IdentityDictionary(),
			analogdata: dataStruct.param['analogdata'],
			other: dataStruct.param['other']
		]);
		
		dataStruct.param['data'].keysValuesDo { |key, val|
			//val.postln;
			//val-dataStruct.param['data'].values.mean
			param['data'].put(key, val-dataStruct.param['data'].values.mean);
		}
	}
}
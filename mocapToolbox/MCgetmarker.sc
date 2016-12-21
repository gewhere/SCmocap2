MCgetmarker {

	var <param;

	*new { | dataStruct, list |
		^super.new.init(dataStruct, list)
	}

	init { | dataStruct, list |
		this.getMarkers(dataStruct, list)
	}

	getMarkers { | dataStruct, list |

		param = param = IdentityDictionary.new;

		param.putPairs([
			datatype: "MoCap data",
			filename: dataStruct.param['filename'],
			nFrames: dataStruct.param['nFrames'],
			nCameras: dataStruct.param['nCameras'],
			nMarkers: list.size,
			frequency: dataStruct.param['frequency'],
			nAnalog: dataStruct.param['nAnalog'],
			timederOrder: 0,
			markerName: list collect: { |i| dataStruct.param['markerName'][i-1] },
			data: IdentityDictionary(),
			analogdata: dataStruct.param['analogdata'],
			other: dataStruct.param['other']
		]);

		dataStruct.param['data'].asSortedArray do: { |i|

			param['data'].put(i[0], []);

			list do: { |j|
				param['data'][i[0]] = param['data'][i[0]].add(i[1][j]);
			}
		}
	}

}
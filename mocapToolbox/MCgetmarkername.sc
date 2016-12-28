MCgetmarkername {

	var <data;

	*new { | dataStruct |
		^super.new.init(dataStruct)
	}

	init { | dataStruct |
		this.getMarkersName(dataStruct)
	}

	getMarkersName { | dataStruct |
		data = dataStruct.param['markerName'];
	}
}
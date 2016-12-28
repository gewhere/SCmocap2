MCmean {
	var <data;

	*new { | dataStruct |
		^super.new.init(dataStruct)
	}

	init { | dataStruct |
		this.getMean(dataStruct)
	}

	getMean { | dataStruct |
		data = dataStruct.param['data'].values.mean;
	}
}
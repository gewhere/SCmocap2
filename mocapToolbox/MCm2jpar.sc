MCm2jpar {

	var <>param;
	
	*new { | nMarkers, markerNum, markerName |
		^super.new.init(nMarkers, markerNum, markerName)
	}

	init { | nMarkers, markerNum, markerName |
		this.loadParameters(nMarkers, markerNum, markerName)
	}

	loadParameters { | nMarkers, markerNum, markerName |
		param = IdentityDictionary.new;

		if(  nMarkers == nil){ nMarkers = () };
		if( markerNum == nil){ markerNum = () };
		if(markerName == nil){ markerName = () };
		
		param.putPairs([
			type: \m2jpar,
			nMarkers: nMarkers,
			markerNum: markerNum,
			markerName: markerName
		]);
		
	}

}
MCanimpar {

	var <>param;

	*new {
		^super.new.init
	}

	init {
		this.loadParameters
	}

	loadParameters {
		param = IdentityDictionary();
		
		param.putPairs([
			type: \animpar,
			scrsize: [600, 400],
			limits: (),
			az: 60,
			el: 0,
			msize: 5,
			colors: "kwww",
			markerColors: (),
			connColors: (),
			traceColors: (),
			numberColors: (),
			cWidth: (),
			tWidth: (),
			conn: (),
			conn2: (),
			trm: (),
			// some missing
			fps: 30,
			output: "/tmp",
			videoFormat: "avi",
			// ....
			pers: "perspective"
		]);

	}
}
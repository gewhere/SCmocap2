MCgnuplot {

	var <>data, <>rawData;
	var <>gnu;
	var <>markers, <>joints, <>conn;
	var <>updater, <>model, <>setValueFunction;

	*new { | mcread, m2jpar, animpar |
		^super.new.init(mcread, m2jpar, animpar)
	}

	init { | mcread, m2jpar, animpar |
		this.initGnuplot(mcread, m2jpar, animpar);
		this.initDependant
	}

	initGnuplot { | mcread, m2jpar, animpar |
		gnu = GNUPlot.new;
		// gnu.sendCmd("unset key; unset tics; unset border; set view 60,60; set multiplot");
		// gnu.sendCmd("splot 0"); // set groundfloor
		this.makeJoints( mcread, m2jpar, animpar );
		rawData = mcread.param['data'];
	}

	initDependant {
		gnu addDependant: this;
	}

	makeJoints { | mcread, m2jpar, animpar |
		var tmp;

		joints = m2jpar.param['markerNum'];
		conn = animpar.param['conn'];
		rawData = mcread.param['data'];

		data = Array.newClear(joints.size);
		"data array: ".post; data.size.postln;
	}

	plotFrame { | frame, type, what | // ask Markers-only, joints, etc
		var tmp, mydata;

		case
		{ (type == \raw) && (what == nil) }{
			gnu.scatter(rawData[frame.asSymbol], 'MoCap markers');
		}
		{ (type == \markers) && (what == nil) }{
			joints.size do: { |i|
				tmp = rawData[frame.asSymbol][ joints[i] - 1 ];
				// check if it is a 2D array (mean will transform 1D array to a number)
				if( tmp.rank == 2){
					data[i] = tmp.mean;
				}{
					data[i] = tmp;
				}
			};
			gnu.sendCmd("set style line 1 lc rgb '#0060ad' lt 1 lw 2 pt 7 ps 1.5");
			gnu.sendCmd("replot");
			gnu.scatter(data, 'MoCap markers');

			"data = ".post; data.postln;
		}
		{ (type == \joints) && (what == nil) }{
			// this has length 20 - as the total number of markers in Demster's model
			tmp = rawData[frame].getJoints(joints);
			"ORIGINAL tmp: ".post; tmp.postln;
			mydata = Array.newClear(tmp.size - 1);
			// gnu.plot3(tmp, label: "mcmocap");
			// do this an animpar stick figure
			try {
				tmp.size do: { |i|
					("tmp[" ++i.asString++ "] = ").post; tmp[i].postln;
					mydata[i] = [tmp[conn[i][0]-1],tmp[conn[i][1]-1]];
					// ns:2 so to get a line of data and an empty line (DOES NOT WORK)
					// http://www.gnuplotting.org/tag/linespoints/
					// check plotting_data2.dat
					//	gnu.replot([tmp[conn[i][0]-1],tmp[conn[i][1]-1]]);
				};
			};
			//"PRE-MYDATA: ".post; mydata.postln;
			//"MYDATA: ".post; mydata.size.postln;
			// set color-line style (blue-ish)
			gnu.sendCmd("set style line 1 lc rgb '#0060ad' lt 1 lw 2 pt 7 ps 1.5");
			gnu.plot3seg(mydata, "", "scmocap", "linespoints ls 1");
		};

	}

	animateFrame { | frame, type, what, dt, skip, label="", style="linespoints" |
		var tmp, mydata, array;

		model = (myValue: mydata);
		setValueFunction = { | value |
			model[\myValue] = value;
			model.changed(\value, value);
			//postf("updateData = %n", updateData);
		};

		if((type == \joints)&&(what == nil)) {
			{
				frame do: { |k|
					if (k+30%2 == 0){
						tmp = rawData[k+30].getJoints(joints);
						//tmp = rawData[(k+30).asSymbol].performList(\getJoints,joints);
						"tmp-size: ".post; tmp.size.postln;

						mydata = Array.newClear(tmp.size - 1);
						// gnu.plot3(tmp, label: "mcmocap");
						// do this an animpar stick figure
						try {
							tmp.size do: { |i|
								("tmp[" ++i.asString++ "] = ").post; tmp[i].postln;
								mydata[i] = [tmp[conn[i][0]-1],tmp[conn[i][1]-1]];
								// http://www.gnuplotting.org/tag/linespoints/
							};
						};
						//"MYDATA: ".post; mydata.postln;
						//"MYDATA-size: ".post; mydata.size.postln;
						//this.changed(\data, mydata)
						//^mydata;

						setValueFunction.value(mydata);
						dt.wait;
					}
				};
			}.fork(AppClock)
		};

		updater = { | who, what, val |
			if(what == \value, {
				//this.window_(val);
				postf("window: %n", val.postln);
				gnu.startMonitor;


				gnu.monitor3({val},0.01);
			})
		};

		model addDependant: updater;
	}

}
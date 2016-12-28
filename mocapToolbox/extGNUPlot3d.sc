+ GNUPlot {

	*plotd3d{ |data,ns=1,label="",style="linespoints"|
		this.bootDefault;
		default.plotd3d( data, ns, label, style );
	}
	// 3D animation -- http://wiki.tcl.tk/13555
	// http://stackoverflow.com/questions/11638636/gnuplot-plotting-positionxyz-vs-time-data-inside-a-specified-space-eg-a-box
	monitor3{ |updateF,dt,length=1,ns=1,skip=1| // id: id of data to monitor, dt: time step, skip: stepsize
		updateFunc = updateF;
		hisdata = Array.fill( length, 0 );
		monrout = Task{
			var cnt = 0;
			inf.do{
				hisdata.pop;
				hisdata = hisdata.addFirst( updateFunc.value );
				cnt = cnt + 1;
				if ( cnt == skip,
					{
						if ( ns > 1, {
							this.plotd3d( hisdata, ns );
						},{
							this.plotd3d( hisdata );
						});
						cnt = 0;
					});
				dt.wait;
			}
		};
	}

	plotd3d{ |data,ns=1,label="",style="linespoints"|
		var delims=[" ", "\n", "\n\n\n"];
		var count=0;
		var str="";
		var tmp="";

		defer{
			pipe.putString("splot ");
			pipe.putString("'-' with "++style++" title \""++label++ns++"\"\n");
			data.do{ |col, i|
				col.do { |sub, k|
					sub.do { |val, l|
						count = count + 1;
						"COUNTER: ".post; count.postln;

						//"[val, l] = ".post; [val, l].postln;

						if( l == 0 ){
							val.do { |item|
								pipe.putString( "%".format(item) ++ delims[0] );
								str = str ++ "%".format(item) ++ delims[0];
							}
						}
						{
							pipe.putString( delims[1] );
							str = str ++ delims[1];
							val.do { |item|
								pipe.putString( "%".format(item) ++ delims[0] );
								str = str ++ "%".format(item) ++ delims[0];
							}

						};
					};
					pipe.putString( delims[2] );
					str = str ++ delims[2];
				};
				pipe.putString( "e" ++ delims[2] );
				str = str ++ "e" ++ delims[2];
				"STRING = ".post; str.postln;
			};
			pipe.flush;
		};
	}

	// plotd3d{ |data, label="", style="linespoints"|
	// 	var delims=[" ", "\n\n", "\n\n\n"];
	// 	defer{
	// 		pipe.putString("splot ");
	// 		pipe.putString("'-' with "++style++" title \""++label++"\"\n");
	// 		data.do{ |col,i|
	// 			col.do { |sub|
	// 				sub.do { |val|
	// 					pipe.putString( "%".format(val) ++ delims[0] );
	// 				};
	// 				pipe.putString( delims[1] );
	// 			};
	// 			// pipe.putString( "e" ++ delims[2]);
	// 			pipe.putString(delims[2]);
	// 			};
	// 		pipe.flush;
	// 	};
	// }

	// // (updateFunction, dt(time interval between updates), length(# of data points to plot), skipFrames)
	// monitor3{ |updateF, dt, length=1, skip=1 |
	// 	updateFunc = updateF;
	// 	hisdata = Array.fill( length, 0 );
	// 	monrout = Task{
	// 		var cnt = 0;
	// 		inf.do{
	// 			hisdata.pop;
	// 			hisdata = hisdata.addFirst( updateFunc.value );
	// 			cnt = cnt + 1;
	// 			if ( cnt == skip,
	// 				{
	// 					this.plotd3d( hisdata );
	// 					cnt = 0;
	// 				});
	// 			dt.wait;
	// 		}
	// 	};
	// }

	// Aucotsi's additions: plot lines individual lines per pair of arrays
	plot3seg {|data, label="", title, style="lines"|
		var fh, tmpname; // = this.createTempFile3( data, ns );
		defer {
			tmpname = this.pr_tmpname;
			this.class.pr_writeTempData4(data, tmpname: tmpname);

			["GNUPlot.plot3 data size: ", data.size].postln;
			title !? {pipe.putString("set title %\n".format(title.asString.quote))};
			pipe.putString("splot % with % title %\n".format(tmpname.asString.quote, style, label.asString.quote));
			lastdata = [ data ];
			pipe.flush;
		}
	}
	// the data for this should be an array-of-arrays-of-arrays, eg:
	// [[p01, p02, p03], [p10, p11, p12], [p20, p21, p22]] where each "pXX" is an array of 3D co-ords.
    // I ADDED THIS
	*pr_writeTempData4 { |data, delims([" ", "\n", "\n\n"]), tmpname|
		// And add exception handling.
		var fh = File.new(tmpname,"w");
		data.do{	|col|
			col.do{|sub|
				sub.do {|val|
					fh.putString(val.asString ++ delims[0]);
				};
				fh.putString(delims[1]);
			};
			fh.putString(delims[2]);
		};
		fh.close;
	}
}

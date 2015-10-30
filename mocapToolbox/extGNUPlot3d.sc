+ GNUPlot {
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
							this.plotd3d( hisdata ); //.flatten );//.flatten, ns); //.flatten, ns );
						});
						cnt = 0;
					});
				dt.wait;
			}
		};
	}

	plotd3d{ |data,ns=1,label="",style="linespoints"|
		var delims=[" ", "\n\n", "\n\n\n"];
		defer{
			pipe.putString("splot ");
			(ns-1).do{ |i|
				//pipe.putString("'-' with "++style++" title \""++label++(i+1)++"\",");
				pipe.putString("'-' with "++style++" title \""++label++(i+1)++"\"\n\n\n");
			};
			pipe.putString("'-' with "++style++" title \""++label++ns++"\"\n");
			if ( ns > 1,
				{
					ns.do{ |id|
						data.do{ |col,i|
							col.do { |sub|
								sub.do { |val|
									//pipe.putString( "%\n".format(val) );
									pipe.putString( "%".format(val) ++ delims[0] );
									//pipe.putString("e\n\n");
								};
								//pipe.putString("\n");
								pipe.putString( delims[1] );
							};
							pipe.putString( "e" ++ delims[2]);
							//pipe.putString("\n");
							"pipe: ".post; pipe.postln;
						};
						pipe.putString( delims[2] );
						//"pipe: ".post; pipe.postln;
					};
				},
				{

					data.do{ |col,i|
						col.do { |sub|
							sub.do { |val|
								//pipe.putString( "%\n".format(val) );
								pipe.putString( "%".format(val) ++ delims[0] );
								//pipe.putString("e\n\n");
							};
							//pipe.putString("\n");
							pipe.putString( delims[1] );
						};
						pipe.putString(  "e" ++  delims[2]);
						//pipe.putString("\n");
						"PIPE1: ".post; pipe.postln;
					};
				});
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
	// *pr_writeTempData4 { |data, delims([" ", "\n", "\n\n"]), tmpname|
	// 	// And add exception handling.
	// 	var fh = File.new(tmpname,"w");
	// 	data.do{	|col|
	// 		col.do{|sub|
	// 			sub.do {|val|
	// 				fh.putString(val.asString ++ delims[0]);
	// 			};
	// 			fh.putString(delims[1]);
	// 		};
	// 		fh.putString(delims[2]);
	// 	};
	// 	fh.close;
	// }
}

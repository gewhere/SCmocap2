g=GNUPlot.new;
// (updateFunction, dt, length(the # of data points to plot, ns(# of rows), skip))
f={[[1.0.rand,1.0.rand,1.0.rand], [1.0.rand,1.0.rand,1.0.rand], [1.0.rand,1.0.rand,1.0.rand]]};
g.monitor3( f, 0.3, 1, 1);
g.setXrange(-1,1);
g.setYrange(-1,1);
g.setZrange(-1,1);
g.startMonitor;
g.stopMonitor;

a.value
a=nil;
a=Array.fill(1, 0);
a.pop;
f = {[1.0.rand2!3, 1.0.rand2!3]};
a.addFirst(f.value);
// [12,3,4].flop
g.plotd3d([[[0.2,0.5,0.8],[0.2,-0.5,-0.8]]], 1);


(
a = [[[0.2,0.5,0.8],[-0.2,-0.5,-0.8]]];
a do: { |col|
	"col: ".post; col.postln;
	col do: { |it|
		"it: ".post; it.postln;
		it do: { |val|
			"val: ".post; val.postln;
		}
	}
}
)

///////////////////////////
	plotd3d{ |data,ns=1,label=""|
		var delims=[" ", "\n", "\n\n"];
		defer{
			pipe.putString("splot ");
			(ns-1).do{ |i|
				pipe.putString("'-' title \""++label++(i+1)++"\",");
			};
			pipe.putString("'-' title \""++label++ns++"\"\n");
			if ( ns > 1,
				{
					ns.do{ |id|
						data.at(id).do{ |it,i|
							it.do{ |val|
								pipe.putString( "%\n".format(val) ++ delims[0] );
							};
							pipe.putString(delims[1]);
						};
							pipe.putString(delims[2]);
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
							pipe.putString("\n");
							//pipe.putString( delims[1] );
						};
						pipe.putString( "e" ++ delims[2] );
						//pipe.putString("e\n");
					};
				});
			pipe.flush;
		};
	}


monitor3{ |updateF,dt,length=3,ns=1,skip=1| // id: id of data to monitor, dt: time step, skip: stepsize
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
							this.plotd3d( hisdata.flop, ns );
						},{
							this.plotd3d( hisdata ); //.flatten );//.flatten, ns); //.flatten, ns );
						});
						cnt = 0;
					});
				dt.wait;
			}
		};
}

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
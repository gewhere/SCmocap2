MCplotframe {

	var <>frame;
	var <>window, <>view;
	var <>mcread;
	var <>data, <>rawData;
	var <>joints, <>markers;

	
	*new { | mcread, m2jpar, animpar, frame |
		^super.new.init(mcread, m2jpar, animpar, frame)
	}

	init { | mcread, m2jpar, animpar, frame |
		this.makeJoints(mcread, m2jpar, animpar, frame);
		this.initFrame(mcread, m2jpar, animpar, frame);
	}

	makeJoints { | mcread, m2jpar, animpar, frame |
		var tmp;
		
		markers = m2jpar.param['markerNum'];
		joints = animpar.param['conn'];
		rawData = mcread.param['data'][frame.asSymbol];

		data = Array.newClear(markers.size);

		markers.size do: { |i|
			tmp = rawData[ markers[i] - 1 ];
			// check if it is a 2D array (mean will transform 1D array to a number)
			if( tmp.rank == 2){
				data[i] = tmp.mean;
			}{
				data[i] = tmp;
			}
		}
	}

	initFrame { | mcread, m2jpar, animpar, frame |
		var offset = 50;
		var width, height;
		width = animpar.param['scrsize'][0];
		height = animpar.param['scrsize'][1];
		
		window = Window.new("MCplotframe", Rect(80, 80, width+offset, height+offset)).front;
		view = MCScatterView3d(window, Rect(10, 10, width, height), data, [-1200, 1700].asSpec); // FIX asSpec
		view.drawMethod = \lineTo;
		view.symbolSize = 1;
		view.symbolColor = Color.blue;
		view.background = Color.black;

		// Y Slider
		Slider(window, Rect(10, height+20, width, 10)).action_{|me|
			view.rotY = me.value * 2pi;
			window.refresh;
		};
		// X Slider
		Slider(window, Rect(width+20, 10, 10, height)).action_{|me|
			view.rotX = me.value * 2pi;
			window.refresh;
		};
		// Z Slider
		Slider(window, Rect(width+40, 10, 10, height)).action_{|me|
			view.rotZ = me.value * 2pi;
			window.refresh;
		};
		
		this.plotMarkers;
	}

	plotMarkers {
		view.isHighlight = true;
		view.highlightColor = Color.green;
		view.highlightSize = 10@10;

		view.highlightRange(0, markers.size);
		window.refresh;
	}

}
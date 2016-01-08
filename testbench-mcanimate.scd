(
~path = "/home/aucotsi/code/sccode/sc3-jyu/data/mocap.tsv";
//t = TabFileReader.read(~path);
t = TabFilePlayer.new( ~path );
t.size;
l = t.readAtLine( 22 );
~str = l.cs;
)

(
// get one frame
l = t.readAtLine( 122 );
~str = l.cs;
~array = ~str.findRegexp("\-?[0-9]+\.[0-9]+");
~newarray = Array.newClear(186);

~array.size.do { | i |
	~newarray[i] = ~array[i][1].interpret;
};
~newarray.postln;
)


(
// make the animation
{
	440 do: { | i |
		if( (i > 22)&&(i%2 == 0) ) {
			l = t.readAtLine( i );
			~str = l.cs;
			~array = ~str.findRegexp("\-?[0-9]+\.[0-9]+");
			~newarray = Array.newClear(186);

			~array.size.do { | i |
				~newarray[i] = ~array[i][1].interpret;
			};
			// ~newarray.postln
			a.data = ~newarray.clump(3);
		};
		0.00001.wait;

		w.refresh;
	}
}.fork(AppClock)
)


(
// plot the connections
var data;
var dimen = 800;
data = ~newarray.clump(3); // {{2.0.rand2}!3}!150;
w = Window.new("3D Scatterplot", Rect(40,40, dimen+50, dimen+50)).front;
a = MCScatterView3d(w, Rect(10,10, dimen, dimen), data, [-1200, 1700].asSpec);
// a.joints= ~joints;
// a.conn = ~conn;
a.drawMethod = \lineTo;
a.symbolSize = 0.5;
a.symbolColor = Color.blue;
a.background = Color.black;

// Y Slider
Slider(w, Rect(10, dimen+20, dimen, 10)).action_{|me|
	a.rotY = me.value * 2pi;
	w.refresh;
};
// X Slider
Slider(w, Rect(dimen+20, 10, 10, dimen)).action_{|me|
	a.rotX = me.value * 2pi;
	w.refresh;
};
// Z Slider
Slider(w, Rect(dimen+40, 10, 10, dimen)).action_{|me|
	a.rotZ = me.value * 2pi;
	w.refresh;
};
)

(
// plot the markers
a.isHighlight = true;
a.highlightColor = Color.green;
a.highlightSize = 10@10;
a.highlightRange(0,61);
w.refresh;
)


~conn = [[11, 12], [11, 17], [11, 13], [10, 11], [1, 10], [13, 14], [17, 18], [14, 15], [18, 19], [15, 16], [19, 20], [1, 2], [1, 6], [2, 3], [3, 4], [4, 5], [6, 7], [7, 8], [8, 9], [ 31, 32 ], [ 31, 37 ], [ 31, 33 ], [ 30, 31 ], [ 21, 30 ], [ 33, 34 ], [ 37, 38 ], [ 34, 35 ], [ 38, 39 ], [ 35, 36 ], [ 39, 40 ], [ 21, 22 ], [ 21, 26 ], [ 22, 23 ], [ 23, 24 ], [ 24, 25 ], [ 26, 27 ], [ 27, 28 ], [ 28, 29 ] ];

~joints = [ [ 14, 15, 16, 17 ], [ 14, 16 ], 18, 20, [ 24, 22 ], [ 15, 17 ], 19, 21, [ 25, 23 ], [ 4, 5, 14, 15, 16, 17 ], [ 4, 5 ], [ 0, 1, 2, 3 ], 4, 8, 10, 12, 5, 9, 11, 13, [ 45, 46, 47, 48 ], [ 45, 47 ], 49, 51, [ 55, 53 ], [ 46, 48 ], 50, 52, [ 56, 54 ], [ 35, 36, 45, 46, 47, 48 ], [ 35, 36 ], [ 31, 32, 33, 34 ], 35, 39, 41, 43, 36, 40, 42, 44 ];

~conn.size;
~joints.size;
(
~path = "/home/aucotsi/code/sccode/sc3-jyu/data/mocap.tsv";
//t = TabFileReader.read(~path);
t = TabFilePlayer.new( ~path );
t.size;
l = t.readAtLine( 22 );
~str = l.cs;
)
//
(
l = t.readAtLine( 122 );
~str = l.cs;
~array = ~str.findRegexp("\-?[0-9]+\.[0-9]+");
~newarray = Array.newClear(186);

~array.size.do { | i |
	~newarray[i] = ~array[i][1].interpret;
};
~newarray.postln;
)
//

(
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
		0.001.wait;

		w.refresh;
	}
}.fork(AppClock)
)


// plot
(
var data;
var dimen = 800;
data = ~newarray.clump(3); // {{2.0.rand2}!3}!150;
w = Window.new("3D Scatterplot", Rect(40,40, dimen+50, dimen+50)).front;
a = MCScatterView3d(w, Rect(10,10, dimen, dimen), data, [-1200, 1700].asSpec);
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
a.isHighlight = true;
a.highlightColor = Color.green;
a.highlightSize = 10@10;
a.highlightRange(0,61);
w.refresh;
)

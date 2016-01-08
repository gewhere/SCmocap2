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


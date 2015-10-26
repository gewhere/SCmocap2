a~path = "/home/aucotsi/code/sccode/sc3-jyu/data/mocap.tsv";
a = MCread(~path);

(
d = a.param['data'];
{
	d.size do: { |i|
		if( d[i.asSymbol].notNil ){
			~data = d[i.asSymbol] / 15;
		};
		wait(1/30);
		u.add(MCcanvas3DItem()
			.color_(Color.green)
			.width_(3)
			.paths_([~data]), // 2nd arg --> connections
			MCcanvas3DItem()
			.color_(Color.blue)
			.width_(3)
			.paths_([~data])
		);
		u.refresh;
	}
}.fork(AppClock);
)





e = a.param['data'][\0];
~data = e / 15;
~data.size;

(
var width = 1200, height = 1000, rate = 0.005;
var w;//, u;

w = Window("3d canvas demo", Rect(128, 64, width, height), false)
    .front;

u = MCcanvas3D(w, Rect(0, 0, width, height))
    .background_(Color.black)
    .scale_(4)
    .perspective_(0.0)
    .distance_(1);

// add DYAD markers
u.add(MCcanvas3DItem()
	.color_(Color.green)
	.width_(3)
	.paths_([~data]), // 2nd arg --> connections
	MCcanvas3DItem()
	.color_(Color.blue)
	.width_(3)
	.paths_([~data])
);

// add cube
u.add(MCcanvas3DItem.cube
	.color_(Color.white)
	.width_(2)
);

// spin canvas on mouse move
u.mouseMoveAction = {|v,x,y,z|
	u.transforms = [
		MCcanvas3D.mRotateX(y / 300 % 2pi),
		MCcanvas3D.mRotateY(x / -300 % 2pi),
		MCcanvas3D.mRotateZ(x / 300 % 2pi)
	];
	u.refresh;
};

u.mouseMoveAction.value(nil, -50, -50); // initial rotation
)
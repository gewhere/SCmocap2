// =====================================================================
// SuperCollider Workspace
// =====================================================================
// [[15, 16, 17, 18], [15, 17], 19, 21, [25, 23], [16, 18], 20, 22, [26, 24], [5, 6, 15, 16, 17, 18], [5, 6], [1, 2, 3, 4], 5, 9, 11, 13, 6, 10, 12, 14] -- markerNum ORIGINAL , below I have include this -1 for appropriate indexing !!!!!

(
~path = "/home/aucotsi/code/sccode/sc3-jyu/data/mocap.tsv";
a = MCread(~path);
a.param.keys;
a.param['data'].size;
a.param.keys;

b = MCm2jpar();
b.param;

b.param.putPairs([
	nMarkers: 40,
	markerNum: #[ [ 14, 15, 16, 17 ], [ 14, 16 ], 18, 20, [ 24, 22 ], [ 15, 17 ], 19, 21, [ 25, 23 ], [ 4, 5, 14, 15, 16, 17 ], [ 4, 5 ], [ 0, 1, 2, 3 ], 4, 8, 10, 12, 5, 9, 11, 13 ],
	markerName: ['p1_root', 'p1_hip_L', 'p1_knee_L', 'p1_ankle_L', 'p1_toe_L', 'p1_hip_R', 'p1_knee_R', 'p1_ankle_R', 'p1_toe_R', 'p1_torso', 'p1_neck', 'p1_head', 'p1_shoulder_L', 'p1_elbow_L', 'p1_wrist_L', 'pl_finger_L', 'p1_shoulder_R', 'p1_elbow_R', 'p1_wrist_R', 'pl_finger_R']
]);
b.param[\markerNum].size;

c = MCanimpar();
c.param.keys;
c.param['conn'] = [[11, 12], [11, 17], [11, 13], [10, 11], [1, 10], [13, 14], [17, 18], [14, 15], [18, 19], [15, 16], [19, 20], [1, 2], [1, 6], [2, 3], [3, 4], [4, 5], [6, 7], [7, 8], [8, 9]];
c.param['conn'].size;
)

g = MCgnuplot(a,b,c);
g.gnu.setXrange(-1000,1000);
g.gnu.setYrange(-1000,1000);
g.gnu.setZrange(0,2000);
g.plotFrame(200, \joints);


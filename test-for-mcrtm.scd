thisProcess.openPorts
a = MCrtm(3001);
a.stopStreaming
a.param['data'].size
a.param['data'].keys
a.param['data'].keys.asArray.sort // sort the keys for sorting the frames
a.param['data'].keys.asArray.sort.last // last frame
// get marker data
~last=a.param['data'].keys.asArray.sort.last
a.param['data'].at(~last)

// get keys
~sortKeys = a.param['data'].at(~last).keys.asArray.sort;

 /// ================================================================
// see the same filename in MAC ~/code/sc3-jyu/test-for-mcrtm.scd

s.quit;
Environment


OSCFunc.trace(true);             // Turn posting on
OSCFunc.trace(false);            // Turn posting on
thisProcess.openUDPPort(3001);
(thisreset: {"thisFunc".postln;}).thisreset

(reset: {"thisFunc".postln;}).reset
Matrix

(
var timestamp = 1e100;
e = ();
d = ();

f = { | msg, time, addr |
	var mySymbol;

	if("/qtm/3d/*".matchRegexp(msg[0].asString) ){
		//"time: % sender: %\nmessage: %\n".postf(time, addr, msg);
		mySymbol = msg[0].asSymbol;

		if(e.currtime - d.currtime < 0.1) {

		};

		e.put(mySymbol, [ msg[1], msg[2], msg[3] ]);
		e.put(\currtime, time);

	};
};
thisProcess.addOSCRecvFunc(f);
)

// stop receiving
thisProcess.removeOSCRecvFunc(f);
d==e
e.currtime

// ===================================
// http://new-supercollider-mailing-lists-forums-use-these.2681727.n2.nabble.com/delay-incoming-values-client-side-td7592090.html#a7592100
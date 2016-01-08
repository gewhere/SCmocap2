s.addr

thisProcess.openUDPPort(9000); // open port 6000 to rcv from pd
thisProcess.openPorts;

n = NetAddr("127.0.0.1", 57120);
m = NetAddr("127.0.0.1", 9000);
r
p
r = OSCFunc({ arg msg, time; [time, msg].postln }, '/bymarker', n);
p = OSCFunc({ arg msg, time; [time, msg].postln }, '/lastmarker/name', n);
q = OSCFunc({ arg msg, time; [time, msg].postln }, '/marker/name', n);
w = OSCFunc({ arg msg, time; [time, msg].postln }, '/lastmarker/bymarker', n);
p.free;


/some examples using CTRNNs
//it's worth trying these examples more than once to see the range of results which vary from networks with interesting outputs to networks that do absolutely nothing

//look at some random CTRNNs with the traditional smoothing function
~params = CTRNNParams.new;
~params.timeStep = 0.05;
~params.numInputNodes=0;
~params.numHiddenNodes = 7;
~params.hSinMixMax=0;

(
//create a random ctrnn
~ctrnn = CTRNN.new(~params);
~outputs = List.new;
//run the network 1000 times
1000.do {
	~ctrnn.update([]);
	//store the outputs of the last 3 nodes
	3.do { |i|
		~outputs.add(~ctrnn.hiddenNode[4 + i].output);
	};
};
~outputs.asArray.plot(numChannels:3);
)

//change the smoothing function to get some more interesting results
~params.hSinMixMax=1;

//generate some more outputs
(
~ctrnn = CTRNN.new(~params);
~outputs = List.new;
//run the network 1000 times
1000.do {
	~ctrnn.update([]);
	//store the outputs of the last 3 nodes
	3.do { |i|
		~outputs.add(~ctrnn.hiddenNode[4 + i].output);
	};
};
~outputs.asArray.plot(numChannels:3);
)


//try sonifying the results
s.boot;
(
SynthDef(\CTRNNhelp1, {
	|freq = 220, amp=1, pan=0|
	var w = SinOsc.ar(freq);
	Out.ar(0, Pan2.ar(w, pan, amp));
}).store;
)

~ctrnnControlledSine = Synth.new(\CTRNNhelp1);

(
//try this block several times to hear some different results
~ctrnnLoop !? {~ctrnnLoop.stop};
//make a random ctrnn
~ctrnn = CTRNN.new(~params);
~ctrnnLoop = {
	inf.do {
		~ctrnn.update([]);
		~ctrnnControlledSine.set(\freq, [20,20000,\exp].asSpec.map(~ctrnn.hiddenNode[4].output));
		~ctrnnControlledSine.set(\amp, ~ctrnn.hiddenNode[5].output);
		~params.timeStep.wait;
	};
}.fork(AppClock);
)


(
~ctrnnControlledSine.free;
~ctrnnLoop.stop;
)

(
//a function to run and display ctrnns
~showCTRNN = {
	|ctrnn,count|
	var outputs = List.new;
	//run the network
	count.do {
		ctrnn.update([]);
		//store the outputs of the last 3 nodes
		ctrnn.hiddenNode.size.do { |i|
			outputs.add(ctrnn.hiddenNode[i].output);
		};
	};
	outputs.asArray.plot(numChannels:ctrnn.hiddenNode.size);
};
)

//create a random CTRNN from a genotype

~params.numHiddenNodes = 3;
~genoType = {1.0.rand} ! CTRNN.getGenotypeLength(~params.numInputNodes, ~params.numHiddenNodes); 
//create a CTRNN
~ctrnn = CTRNN.new(~params, ~genoType);

~showCTRNN.(~ctrnn, 1000);

//create mutations from the parent genotype
~ctrnn.mutate(0.1, 0.001);
~showCTRNN.(~ctrnn, 1000);
~ctrnn.mutate(0.1, 0.001);
~showCTRNN.(~ctrnn, 1000);

//make the parent genotype the same as the current mutation
~ctrnn.mutateForward;

//mutate from this the new parent
~ctrnn.mutate(0.1, 0.001);
~showCTRNN.(~ctrnn, 1000);


///////////////////////////////////////////////////////////
// Evolving drum patterns

(
SynthDef(\memDrum, {
	|tension=0.05, loss=0.99, amp=0.5|
	var e = Env.perc(0.01, 0.1, amp);
	var eg = EnvGen.ar(e, doneAction:0) * BrownNoise.ar(0.2);
	var w = MembraneHexagon.ar(eg, tension, loss);
	Out.ar(0, w.dup);
	DetectSilence.ar(w, doneAction:2);
}).store;
)

~params = CTRNNParams.new;
~params.timeStep = 0.05;
~params.numInputNodes=0;
~params.numHiddenNodes = 50;  //lots of nodes to make the output more interesting
~params.hSinMixMax=1;

(
//try this block several times to hear some different results
~ctrnnLoop !? {~ctrnnLoop.stop};
//make a random ctrnn
~ctrnn = CTRNN.new(~params);
~lastOutput1=0;
~lastOutput2=0;
~lastOutput3=0;
~ctrnnLoop = {
	inf.do {
		~ctrnn.update([]);
		if ((~lastOutput1 - ~ctrnn.hiddenNode[5].output).abs > 0.7, {
			TempoClock.default.schedAbs(TempoClock.default.nextTimeOnGrid(0.125), {
				Synth(\memDrum, [\tension, 0.03, \loss, 0.9995, \amp, ~ctrnn.hiddenNode[1].output + 1 / 4]);
			});
		});
		if ((~lastOutput2 - ~ctrnn.hiddenNode[6].output).abs > 0.5, {
			TempoClock.default.schedAbs(TempoClock.default.nextTimeOnGrid(0.125), {
				Synth(\memDrum, [\tension, 0.05, \loss, 0.9995, \amp, ~ctrnn.hiddenNode[2].output + 1 / 4]);
			});
		});
		if ((~lastOutput3 - ~ctrnn.hiddenNode[7].output).abs > 0.5, {
			TempoClock.default.schedAbs(TempoClock.default.nextTimeOnGrid(0.125), {
				Synth(\memDrum, [\tension, 0.06, \loss, 0.999, \amp, ~ctrnn.hiddenNode[3].output + 1 / 4]);
			});
		});
		~lastOutput1 = ~ctrnn.hiddenNode[5].output;
		~lastOutput2 = ~ctrnn.hiddenNode[6].output;
		~lastOutput3 = ~ctrnn.hiddenNode[7].output;
		0.1.wait;
	};
}.fork;
)

//evolve from here:
~ctrnn.mutate(0.1, 0.0001);
~ctrnn.mutateForward;

~ctrnnLoop.stop;


///////////////////////////////////////////////////////////////////
// a more complex synthesis examples

(
SynthDef(\FourOscPad, 
{
	|freq = 220, o1vol=1, o2vol=1, o3vol=1, o4vol=1, gate=1.0, amp=1.0,
	o1detune = 0, o2detune = 0, o3detune = 0, o4detune = 0, 
	o1pw = 0.5, o2pw = 0.5, o3pw = 0.5, pwmFreq = 0,
	fCutoff = 20000, fRes = 0, fLow = 1.0, fHigh = 0, fBand = 0, fNotch=0, fPeak = 0,
	rFreq=20000, rRes=0, filtBal=0.5, filt1Pan=0, filt2Pan=0|
	var o1, o2, o3, o4, w, w2, aEnv,pwm;
	pwm = LFTri.kr(pwmFreq, 0);
	o1 = LFPulse.ar(freq * (1.0 + o1detune), 0, o1pw * pwm, o1vol);
	o2 = LFSaw.ar(freq * (1.0 + o2detune), 0, o2pw * pwm, o2vol);
	o3 = VarSaw.ar(freq * (1.0 + o3detune), 0, o3pw * pwm, o3vol);
	o4 = LFSaw.ar(freq * (1.0 + o4detune), 0, o4vol);
	w = o1 + o2 + o3 + o4 / (o1vol +o2vol +o3vol +o4vol);
	w2 = Resonz.ar(w, rFreq, rRes);
	w = SVF.ar(w, fCutoff, fRes, fLow, fBand, fHigh, fNotch, fPeak);
	w = Pan2.ar(w, filt1Pan, filtBal) + Pan2.ar(w2, filt2Pan, 1-filtBal);
	Out.ar(0, w);
}).store;
)

//play a major 7th chord
~all = ([30,34,37,41,42] + 24).collect({|v,i| Synth(\FourOscPad, [\freq, v.midicps, \aEnvA, 3, \aEnvR, 3])});

//init a ctrnn
~params = CTRNNParams.new;
~params.timeStep = 0.01;
~params.numInputNodes=0;
~params.numHiddenNodes = 25;
~params.hSinMixMax=1;

(
//try this block several times to hear some different results
~ctrnnLoop !? {~ctrnnLoop.stop};
//make a random ctrnn
~ctrnn = CTRNN.new(~params);
~ctrnnLoop = {
	inf.do {
		~ctrnn.update([]);
		~all.do({|v| v.set(\fCutoff, [20,20000,\exp].asSpec.map(~ctrnn.hiddenNode[4].output + 1 / 2))});
		~all.do({|v| v.set(\o1Vol, ~ctrnn.hiddenNode[0].output + 1 / 2)});
		~all.do({|v| v.set(\o2Vol, ~ctrnn.hiddenNode[1].output + 1 / 2)});
		~all.do({|v| v.set(\o3Vol, ~ctrnn.hiddenNode[2].output + 1 / 2)});
		~all.do({|v| v.set(\o4Vol, ~ctrnn.hiddenNode[3].output + 1 / 2)});
		~all.do({|v| v.set(\o1detune, [-0.01,0.01].asSpec.map(~ctrnn.hiddenNode[4].output + 1 / 2))});
		~all.do({|v| v.set(\o2detune, [-0.01,0.01].asSpec.map(~ctrnn.hiddenNode[5].output + 1 / 2))});
		~all.do({|v| v.set(\o3detune, [-0.01,0.01].asSpec.map(~ctrnn.hiddenNode[6].output + 1 / 2))});
		~all.do({|v| v.set(\o4detune, [-0.01,0.01].asSpec.map(~ctrnn.hiddenNode[7].output + 1 / 2))});
		~all.do({|v| v.set(\o1pw, ~ctrnn.hiddenNode[8].output + 1 / 2)});
		~all.do({|v| v.set(\o2pw, ~ctrnn.hiddenNode[9].output + 1 / 2)});
		~all.do({|v| v.set(\o3pw, ~ctrnn.hiddenNode[10].output + 1 / 2)});
		~all.do({|v| v.set(\o4pw, ~ctrnn.hiddenNode[11].output + 1 / 2)});
		~all.do({|v| v.set(\rFilt, [20,20000, \exp].asSpec.map(~ctrnn.hiddenNode[12].output + 1 / 2))});
		~all.do({|v| v.set(\rRes, ~ctrnn.hiddenNode[13].output + 1 / 2)});
		~all.do({|v| v.set(\filtBal, ~ctrnn.hiddenNode[14].output + 1 / 2)});
		~all.do({|v| v.set(\filt1Pan, ~ctrnn.hiddenNode[15].output)});
		~all.do({|v| v.set(\filt2Pan, ~ctrnn.hiddenNode[16].output)});
		~all.do({|v| v.set(\fRes, ~ctrnn.hiddenNode[17].output + 1 / 4 + 0.5)});
		~all.do({|v| v.set(\fLow, ~ctrnn.hiddenNode[18].output + 1 / 2)});
		~all.do({|v| v.set(\fHigh, ~ctrnn.hiddenNode[19].output + 1 / 2)});
		~all.do({|v| v.set(\fBand, ~ctrnn.hiddenNode[20].output + 1 / 2)});
		~all.do({|v| v.set(\fNotch, ~ctrnn.hiddenNode[21].output + 1 / 2)});
		~all.do({|v| v.set(\fPeak, ~ctrnn.hiddenNode[22].output + 1 / 2)});
		~params.timeStep.wait;
	};
}.fork;
)

//listen to mutations
~ctrnn.mutate(0.05, 0.0001);
//move forward a generation
~ctnnn.mutateForward;


~ctrnnLoop.stop;
~all.do({|v| v.free});

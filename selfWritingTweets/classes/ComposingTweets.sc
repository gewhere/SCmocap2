ComposingTweets {

	var <ugen, <ugenArgs, <defaultArgs;
	
	*new {
		^super.new.init
	}

	init {
		this.chooseUGen
	}

	chooseUGen {
		ugen = UGen.allSubclasses.choose.asString;
		ugenArgs = ugen.class.findRespondingMethodFor(\ar|\kr|\ir).argNames;
		//defaultArgs =
		
		this.chooseArgs(ugen)
	}

	chooseArgs { | which |
		if(ugenArgs.includes(\freq)){
			which
		}{
			this.chooseUGen
		}
		
	}
	
}

/*
u = UGen.allSubclasses.choose.asString
u.class
u.interpret.class.findRespondingMethodFor(\ar).argNames;
u.interpret.ar.inputs


x = LFDNoise3.kr; 
y = x.inputs.collect({ arg in,ini; x.argNameForInputAt(ini) ? ini.asSymbol });


LFDNoise3.ar.dumpArgs
LFNoise0.ar.dumpArgs
SinOsc.ar.dumpArgs
SinOsc.ar.numInputs
LFNoise0.ar.numInputs
LFNoise0.ar.methodSelectorForRate

SinOsc.ar.range.isValidUGenInput
SinOsc.ar.inputs
SinOsc.ar.checkNInputs(3)

dumpArgs {
	" ARGS:".postln;
	inputs.do({ arg in,ini;
		("   " ++ (this.argNameForInputAt(ini) ? ini.asString)++":" + in + in.class).postln
	});
}


(
d=nil;
d = IdentityDictionary.new;
UGen.allSubclasses do: { |ugen|
	var arr;
	arr = ugen.class.findRespondingMethodFor(\ar).argNames;
	arr do: { |i|
		d.put(i,
			if(d[i].isNil){
				1
			}{
				d[i] + 1
			}
		)
	};
}
)

//d.keys.asArray.size

d.values.sort.reverse do: { |val|
	//val.post; ": ".post; //d.findKeyForValue(val).postln;
	d.keysValuesDo { |key, value|
		if(value == val){
			val.post; ": ".post; key.postln;
		}
	}
}

d.keys.size

d.keysValuesDo { |key, value|
	if(value == 14){
		key.postln;
	}
}

d.removeAt('in')
d.findKeyForValue(d.values.maxItem)
d.values.maxItem
*/
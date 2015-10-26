/*

Top class for Live Hardware Coding simulator


a = LHC.new.start;

a = LHC((symbol: { (degree: 0).play }, counter: { (degree: 10).play }));
a.start;

a = LHC((
	symbol: { | self, symbol | (degree: (A: 11, B: 12, C: 13, D: 14)[symbol]).play },
	counter: { | self, counter | (degree: counter).play }
)).start;

a = LHC((
	symbol: { | self, symbol | (degree: (A: 11, B: 12, C: 13, D: 14)[symbol]).play },
	counter: { | self, counter | if (counter > 0) { (degree: counter).play } }
)).start;


//:
a = LHC((
	symbol: { | self, symbol |
		self.soundOn.postln;
		if (self.soundOn) {
			(degree: (A: 11, B: 12, C: 13, D: 14)[symbol]).play
		}
	},
	counter: { | self, counter |
		if (counter > 0) {
			(degree: counter).play;
			self.soundOn = true;
			self.soundOn.postln;
		}{
			self.soundOn = false
		}
	}
)).start;
a.loadChord

*/

LHC {

	var <window;
	var <counterDisplay, <encoderDisplay, <>bit0, <>bit1, <>bit2, <resetbutton;
	var <positiveEdgeDisplay;
	var <bit0numbox, <bit1numbox, <bit2numbox;
	var <currentToken, <previousToken;
	var <tokenA, <tokenB, <tokenC, <tokenD, <tokenE, <tokenF, <tokenG;

	var <>waitTime = 0.5, <routine;
	var <>counter = 0, <>input = 0;
	var <>knob; // the knob for the vcl
	var gdelta = 0;

	var <fsmDecoder;
	var <>symbolicStream ="";
	var <>loadChord;

	*new { | player | // player: sound algorithm
		^super.new.init(player);
	}

	init { | player |
		this.makeFsmDecoder;
		this.addPlayer(player);
	}

	makeFsmDecoder {
		fsmDecoder = FSMdecoder.new;
		fsmDecoder addDependant: this;
	}

	addPlayer { | playerEvent |
		fsmDecoder addDependant: LHCplayer(playerEvent ?? { () });
	}

	start {
		this.makeWindow;
		this.makeRoutine;
	}

	makeWindow { if (window.isNil) { this.prMakeWindow } { window.front } }

	prMakeWindow {
		window = Window.new("",Rect(318, 309, 506, 447)).front;
		window.onClose = { this.windowClosed };
		// new elements on GUI
		// KNOB for the vclk
		//knob = Knob.new(window, Rect(400, 290, 32, 32));
		// TOKENS
		tokenA = Button(window, Rect(20, 50, 40, 20))
			.states_([["D", Color.white, Color.black], ["D", Color.white, Color.blue]]);
		tokenB = Button(window, Rect(60, 50, 40, 20))
			.states_([["B+D", Color.white, Color.black], ["B+D", Color.white, Color.blue]]);
		tokenC = Button(window, Rect(100, 50, 40, 20))
			.states_([["C+D", Color.white, Color.black], ["C+D", Color.white, Color.blue]]);
		tokenD = Button(window, Rect(140, 50, 70, 20))
			.states_([["(B+C+)+D", Color.white, Color.black], ["(B+C+)+D", Color.white, Color.blue]]);
		tokenE = Button(window, Rect(210, 50, 70, 20))
			.states_([["(C+B+)+D", Color.white, Color.black], ["(C+B+)+D", Color.white, Color.blue]]);
		tokenF = Button(window, Rect(280, 50, 100, 20))
			.states_([["(B+C+)+B+D", Color.white, Color.black], ["(B+C+)+B+D", Color.white, Color.blue]]);
		tokenG = Button(window, Rect(380, 50, 100, 20))
			.states_([["(C+B+)+C+D", Color.white, Color.black], ["(C+B+)+C+D", Color.white, Color.blue]]);
		// PREVIOUS TOKEN
		previousToken = NumberBox.new(window,Rect(20, 10, 200, 20))
			.font_(Font("Monaco", 12))
			.action_{|v| }
			.enabled_(false);
		StaticText.new(window,Rect(20, 30, 100, 20))
			.string_("previous_token")
			.action_{|v| };
		// CURRENT TOKEN
		currentToken = NumberBox.new(window,Rect(250, 10, 200, 20))
			.font_(Font("Monaco", 12))
			.action_{|v| }
			.enabled_(false);
		StaticText.new(window,Rect(250, 30, 100, 20))
			.string_("current_token")
			.action_{|v| };
		// END OF TOKENS
		positiveEdgeDisplay = Button(window, Rect(20, 100, 30, 30))
			.states_([[" ", Color.black, Color.black], [" ", Color.yellow, Color.yellow]]);
		counterDisplay = NumberBox.new(window,Rect(100, 100, 100, 100))
			.font_(Font("Monaco", 60))
			.action_{|v| }
			.enabled_(false);
		StaticText.new(window,Rect(100, 200, 100, 20))
			.string_("COUNTER")
			.action_{|v| };
		encoderDisplay = TextField.new(window,Rect(225, 100, 100, 100))
			.action_{|v| }
			.font_(Font("Monaco", 60))
			.enabled_(false);
		StaticText.new(window,Rect(230, 200, 100, 20))
			.string_("ENCODER")
			.action_{|v| };

		bit0 = Button.new(window,Rect(280, 300, 100, 20))
			.states_([ [ "0", Color(1.0), Color() ], [ "1", Color(), Color(1.0) ] ])
			.action_{| v |  };
		bit1 = Button.new(window,Rect(170, 300, 100, 20))
			.states_([ [ "0", Color(1.0), Color() ], [ "1", Color(), Color(1.0) ] ])
			.action_{| v |  };
		bit2 = Button.new(window,Rect(60, 300, 100, 20))
			.states_([ [ "0", Color(1.0), Color()  ], [ "1", Color(), Color(1.0) ] ])
			.action_{| v |  };
		resetbutton = Button.new(window,Rect(430, 220, 50, 50))
			.states_([ [ "RESET", Color(1.0), Color() ], [ "RESET", Color(), Color(1.0) ] ])
			.action_{| v | counter = 0; };
		window.view.keyDownAction =  { | view, char, modifiers, unicode, keycode |
//			keycode.postln;
			switch (char,
				$d, { bit0.valueAction = 1 - bit0.value },
				$s, { bit1.valueAction = 1 - bit1.value  },
				$a, { bit2.valueAction = 1 - bit2.value  },
				$r, { resetbutton.value = 1 - resetbutton.value }
			);
//			[char, modifiers, unicode, keycode].postln;
		}
	 }

	windowClosed {
		window = nil;
		this.stopSynthsAndProcesses;
		fsmDecoder.releaseDependants;
	}

	stopSynthsAndProcesses {
		routine.stop;
		routine = 0;
	}

	makeRoutine {
		if (routine.isNil) {
			routine = {
				loop {
					this.flashPositiveEdgeDisplay;
					input = ([bit0.value, bit1.value, bit2.value] * [1, 2, 4]).sum;
					if (resetbutton.value > 0) {
						counter = 0;
						fsmDecoder.changed(\reset);
						"fsmDecoder should now broadcast change RESET".postln;
					}{
						counter = counter + input % 8;
						fsmDecoder.changed(\counter, counter);
					};
					counterDisplay.value = counter;
					this.calculateFSMstate(counter);
					waitTime.wait;
				}
			}.fork(AppClock);
		}
	}

	flashPositiveEdgeDisplay {
		{
			positiveEdgeDisplay.value = 1;
			(waitTime / 3).wait;
			positiveEdgeDisplay.value = 0;
		}.fork(AppClock);
	}

	flashTokens {
		case
			{ this.loadChord === \tokenA } {
				tokenA.value = 1;
				tokenB.value = 0;
				tokenC.value = 0;
				tokenD.value = 0;
				tokenE.value = 0;
				tokenF.value = 0;
				tokenG.value = 0;
			}
			{ this.loadChord === \tokenB } {
				tokenA.value = 0;
				tokenB.value = 1;
				tokenC.value = 0;
				tokenD.value = 0;
				tokenE.value = 0;
				tokenF.value = 0;
				tokenG.value = 0;
			}
			{ this.loadChord === \tokenC } {
				tokenA.value = 0;
				tokenB.value = 0;
				tokenC.value = 1;
				tokenD.value = 0;
				tokenE.value = 0;
				tokenF.value = 0;
				tokenG.value = 0;
			}
			{ this.loadChord === \tokenD } {
				tokenA.value = 0;
				tokenB.value = 0;
				tokenC.value = 0;
				tokenD.value = 1;
				tokenE.value = 0;
				tokenF.value = 0;
				tokenG.value = 0;
			}
			{ this.loadChord === \tokenE } {
				tokenA.value = 0;
				tokenB.value = 0;
				tokenC.value = 0;
				tokenD.value = 0;
				tokenE.value = 1;
				tokenF.value = 0;
				tokenG.value = 0;
			}
			{ this.loadChord === \tokenF } {
				tokenA.value = 0;
				tokenB.value = 0;
				tokenC.value = 0;
				tokenD.value = 0;
				tokenE.value = 0;
				tokenF.value = 1;
				tokenG.value = 0;
			}
			{ this.loadChord === \tokenG } {
				tokenA.value = 0;
				tokenB.value = 0;
				tokenC.value = 0;
				tokenD.value = 0;
				tokenE.value = 0;
				tokenF.value = 0;
				tokenG.value = 1;
			};
	}

	calculateFSMstate { | counter |
		// current state + input = next state + output
		{
			counter.asBinaryString(3) do: { | digit |
				fsmDecoder input: (digit == $1).binaryValue;
				(waitTime / 3).wait;
			};
		}.fork(AppClock);
	}

	update { | who, what, value |
//		postf("% updated: % to: %\n", who, what, value);
		switch (what,
			\symbol, {
				// update encoder's display
				encoderDisplay.string = value.asString;
				// ensure there are no $A
				if(value.asString != "A") {
					symbolicStream = symbolicStream ++ value.asString;
					currentToken.string = symbolicStream;
				};
				// check end marker
				if(value.asString == "D"){
					loadChord = LHCregexp(symbolicStream);
					previousToken.string = symbolicStream;
					// flash tokens GUI
					this.flashTokens;
					"loadChord: ".post; loadChord.postln;
					"stream: ".post; symbolicStream.postln;
					// reset stream
					symbolicStream = "";
				}
			},
			\state, { }
		);
	}

}

LHCregexp {
	var <>list;

	*new { | str |
		^super.new.init(str);
	}

	init { | str |
		this.tokenList;
		^this.detectRegExp(str);
	}

	tokenList {
		list = ["^D", "^B+D", "^C+D", "^(B+C+)+D", "^(C+B+)+D", "^(B+C+)+B+D", "^(C+B+)+C+D"];
	}

	detectRegExp { | str |
		case
		{ list[0].matchRegexp(str) } { ^\tokenA }
		{ list[1].matchRegexp(str) } { ^\tokenB }
		{ list[2].matchRegexp(str) } { ^\tokenC }
		{ list[3].matchRegexp(str) } { ^\tokenD }
		{ list[4].matchRegexp(str) } { ^\tokenE }
		{ list[5].matchRegexp(str) } { ^\tokenF }
		{ list[6].matchRegexp(str) } { ^\tokenG };
	}

}
/*

LHCregexp("CD")

*/
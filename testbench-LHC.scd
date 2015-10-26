a = LHCV().start // see LHC example for musical example
a.counterOut // returns the current value of the Counter
a.vclk // returns the current value of the Var Clk rate (in Hz)
a.counterIn // returns the current input
// set main clk (latched)
a.setClk_(value)

//
(
a = LHCV().start;
)

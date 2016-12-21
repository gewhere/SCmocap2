//"PRINT A LINE !!!!!!!!!!!!!!!!!!!!".postln;
{
 //    format("python -c %import os
 //print('HELLO' + os.linesep + 'THERE')%", '"'.asString, '"'.asString).unixCmd;
     s.waitForBoot {
         { SinOsc.ar([340,342]) }.play;
     };
     9.wait;
     s.quit;
     0.exit
}.fork(AppClock)

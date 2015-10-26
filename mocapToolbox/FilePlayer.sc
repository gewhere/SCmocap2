// by nescivi
// http://new-supercollider-mailing-lists-forums-use-these.2681727.n2.nabble.com/FileReader-go-to-line-td3089434.html
FilePlayer : FileReader {

	var <>currentLine = 0;
	var <>lineMap;

	*new { | pathOrFile, skipEmptyLines=false, skipBlanks=false,  delimiter |
		var stream;
		if (pathOrFile.isKindOf(File) ) { stream = pathOrFile }  { stream =  File(pathOrFile, "r") };
		if (stream.isOpen.not) { warn("FileReader: file" + pathOrFile + "not found.") ^nil };
		^super.newCopyArgs(stream, skipEmptyLines, skipBlanks,  delimiter ? this.delim).myInit;
	}

	myInit{
		lineMap = Order.new;
		lineMap.put( 0, 0 );
	}

	reset{
		currentLine = 0;
		super.reset;
	}

	next{
		var res = super.next;
		this.setCurrentLine( currentLine + 1 );
		^res;
	}

	/* should not implement here
	nextN{ |n|
		var res = super.nextN(n);
		this.setCurrentLine( currentLine + n );
		^res;
	}

	skipNextN{ |n|
		var res = super.skipNextN(n);
		this.setCurrentLine( currentLine + n );
		^res;
	}
	*/

	setCurrentLine{ |cl|
		currentLine = cl;
		lineMap.put( currentLine, stream.pos );
		[ currentLine, stream.pos].postln;
	}

	goToLine{ |line|
		var ind, lmap;
		var pos = lineMap.at( line );
		if ( pos.notNil,
			{
				stream.pos = pos;
				currentLine = line;
			},
			{
				ind = lineMap.slotFor( line );
				// ind is now the index into the indices for the highest line number before the one we know.
				lmap = lineMap.indices.at( ind );
				stream.pos = lineMap.at( lmap );
				[ind, lmap, stream.pos, line, line-lmap ].postln;
				this.skipNextN( line - lmap );
			}
		);
	}

	readAtLine{ |line|
		this.goToLine( line );
		^this.next;
	}

}

TabFilePlayer : FilePlayer {
	classvar <delim = $\t;
}

CSVFilePlayer : FilePlayer {
	classvar <delim = $,;
}

SemiColonFilePlayer : FilePlayer {
	classvar <delim = $;;
}

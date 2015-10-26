+ Array {
	getMarkersFrame { | index |
		var array, data;
		
		data = Array.newClear(index.size);
		
		index.size do: { | i |
			array = this[index[i]];

			// check if it is a 2D array (mean will transform 1D array to a number)
			if( array.rank == 2){
				data[i] = array.mean;
			}{
				data[i] = array;
			};
		};
		^data
	}
}
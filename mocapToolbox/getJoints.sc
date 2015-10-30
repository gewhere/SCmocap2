+ Array {
	getJoints { | joints |
		var array, data;
		
		data = Array.newClear(joints.size);
		
		joints.size do: { | i |
			array = this[joints[i]];

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
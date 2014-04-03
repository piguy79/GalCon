exports.moveEquals = function(actual, expected){
	if(actual.player == expected.player && actual.from == expected.from
	   && actual.from == expected.from && actual.fleet == expected.fleet
	   && actual.duration == expected.duration){
		return true;
	}
	
	return false;
}
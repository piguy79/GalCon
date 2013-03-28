exports.moveEquals = function(actual, expected){
	if(actual.player == expected.player && actual.fromPlanet == expected.fromPlanet
	   && actual.fromPlanet == expected.fromPlanet && actual.fleet == expected.fleet
	   && actual.duration == expected.duration){
		return true;
	}
	
	return false;
}
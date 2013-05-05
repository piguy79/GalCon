var SPEED_BOOST = 0.4;

var speedIncreasePlanetsHeldByPlayer = function(playerHandle, planets){
	var count = 1;
		
	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if((planet.ability && planet.ability == 'SPEED') && planet.ownerHandle == playerHandle){
			count = count + SPEED_BOOST;
		}
	}
	
	return count;
}


exports.addPlanetAbilities = function(planetsFarFromHomes){

	var speedIncreasePlanetSet = false;
	
	while(!speedIncreasePlanetSet){
	
		var planetToAddTo = planetsFarFromHomes[0];
		planetToAddTo.shipRegenRate = 1;
		
		planetToAddTo.ability = 'SPEED';
		speedIncreasePlanetSet = true;
		
	}
}

exports.applyMovesToGame = function(game){

	var i = game.moves.length;
	while (i--) {
		var move = game.moves[i];
		var speedIncrease = speedIncreasePlanetsHeldByPlayer(move.playerHandle, game.planets);
		move.duration = move.duration - speedIncrease;
		if (move.duration <= 0) {
			game.applyMoveToPlanets(move);
			game.moves.splice(i, 1)
		}
	}

}



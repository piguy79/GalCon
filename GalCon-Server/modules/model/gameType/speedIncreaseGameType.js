var SPEED_BOOST = 0.4;
var SPEED_ABILITY = 'SPEED';
var SPEED_SHIP_REGEN_RATE = 1;
var SPEED_SHIP_NUMBER = 10;

var speedIncreasePlanetsHeldByPlayer = function(playerHandle, planets){
	var count = 1;
		
	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if((planet.ability && planet.ability == SPEED_ABILITY) && planet.ownerHandle == playerHandle){
			count = count + SPEED_BOOST;
		}
	}
	
	return count;
}


exports.addPlanetAbilities = function(planetsFarFromHomes){

	for(var i=0; i < planetsFarFromHomes.length; i++){
		var planetToAddTo = planetsFarFromHomes[i];
		
		if(!planetToAddTo.ability){
			planetToAddTo.shipRegenRate = SPEED_SHIP_REGEN_RATE;
			planetToAddTo.numberOfShips = SPEED_SHIP_NUMBER;
			planetToAddTo.ability = SPEED_ABILITY;
			break;
		}
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



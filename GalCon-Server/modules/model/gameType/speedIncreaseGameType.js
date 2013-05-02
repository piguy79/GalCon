var SPEED_BOOST = 0.4;
var SPEED_ABIBILITY = 'SPEED';
var SPEED_SHIP_REGEN_RATE = 1;
var SPEED_SHIP_NUMBER = 10;

var speedIncreasePlanetsHeldByPlayer = function(player, planets){
	var count = 1;
		
	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if((planet.ability && planet.ability == SPEED_ABIBILITY) && planet.owner == player){
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
			planetToAddTo.ability = SPEED_ABIBILITY;
			break;
		}
	}
	
}

exports.applyMovesToGame = function(game){

	var i = game.moves.length;
	while (i--) {
		var move = game.moves[i];
		var speedIncrease = speedIncreasePlanetsHeldByPlayer(move.player, game.planets);
		move.duration = move.duration - speedIncrease;
		if (move.duration <= 0) {
			game.applyMoveToPlanets(move);
			game.moves.splice(i, 1)
		}
	}

}



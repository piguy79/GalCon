var abilityBasedGameType = require('./abilityBasedGameType');

var SPEED_BOOST = 0.5;
var SPEED_ABIBILITY = 'SPEED';


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
	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,SPEED_ABIBILITY);
}

exports.applyMovesToGame = function(game){

	var i = game.moves.length;
	while (i--) {
		var move = game.moves[i];
		var speedIncrease = speedIncreasePlanetsHeldByPlayer(move.player, game.planets);
		move.duration = move.duration - speedIncrease;
		if (move.duration <= 0) {
			game.applyMoveToPlanets(game, move);
			game.moves.splice(i, 1)
		}
	}

}



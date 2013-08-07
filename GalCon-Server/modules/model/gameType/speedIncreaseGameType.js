var abilityBasedGameType = require('./abilityBasedGameType');

var SPEED_ABILITY = 'speedModifier';

var speedIncreasePlanetsHeldByPlayer = function(config, playerHandle, planets){
	var count = 1;
		
	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if((planet.ability && planet.ability == SPEED_ABILITY) && planet.ownerHandle == playerHandle){
			count = count + parseFloat(config.values[SPEED_ABILITY]);
		}
	}
	
	return count;
}


exports.addPlanetAbilities = function(planetsFarFromHomes){
	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,SPEED_ABILITY);
}

exports.applyMovesToGame = function(game,multiplierMap){
	var i = game.moves.length;
	while (i--) {
		var move = game.moves[i];
		var speedIncrease = speedIncreasePlanetsHeldByPlayer(game.config, move.playerHandle, game.planets);
		move.duration = move.duration - speedIncrease;
		if (move.duration <= 0) {
			game.applyMoveToPlanets(game, move, multiplierMap);
			move.executed = true;
		}
	}
}

var abilityBasedGameType = require('./abilityBasedGameType'),
	standardGameType = require('./standardGameType');

var speedIncreasePlanetsHeldByPlayer = function(config, playerHandle, planets, game){
	var count = 1;
		
	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if((planet.ability && planet.ability == abilityBasedGameType.SPEED_ABILITY) && planet.ownerHandle == playerHandle){
			var speedIncrease = parseFloat(config.values[abilityBasedGameType.SPEED_ABILITY]) + abilityBasedGameType.harvestEnhancement(playerHandle, game);
			count = count + speedIncrease;
		}
	}
	
	return count;
}


exports.addPlanetAbilities = function(planetsFarFromHomes) {
	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,[abilityBasedGameType.SPEED_ABILITY]);
}

exports.applyMovesToGame = function(game, multiplierMap) {
	standardGameType.applyMovesToGame(game, multiplierMap, function(move) {
		return move.duration - speedIncreasePlanetsHeldByPlayer(game.config, move.handle, game.planets, game);
	});
}

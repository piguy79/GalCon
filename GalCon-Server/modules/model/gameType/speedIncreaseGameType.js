var abilityBasedGameType = require('./abilityBasedGameType'),
	standardGameType = require('./standardGameType');

var speedIncreasePlanetsHeldByPlayer = function(config, handle, planets, game){
	var count = 1;
	var abilityDetected = false;
		
	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if((planet.ability && planet.ability == abilityBasedGameType.SPEED_ABILITY) && planet.handle === handle){
			var speedIncrease = parseFloat(config.values[abilityBasedGameType.SPEED_ABILITY]);
			count = count + speedIncrease;
			abilityDetected = true;
		}
	}
	
	if(abilityDetected){
		count += abilityBasedGameType.harvestEnhancement(handle, game, abilityBasedGameType.SPEED_ABILITY)
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

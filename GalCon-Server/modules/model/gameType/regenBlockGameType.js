var abilityBasedGameType = require('./abilityBasedGameType');

var REGEN_BLOCK_ABILITY = 'blockModifier';


exports.addPlanetAbilities = function(planetsFarFromHomes){

	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,REGEN_BLOCK_ABILITY);
	
}

exports.determineIfAnOpponentHasTheRegenBlock = function(game, playerHandle){

	for(var  i = 0; i < game.planets.length; i++){
		var planet = game.planets[i];
		if(planet.handle && planet.handle != playerHandle && (planet.ability && planet.ability == REGEN_BLOCK_ABILITY)){
			return true;
		}
	}
	
	return false;
	
}




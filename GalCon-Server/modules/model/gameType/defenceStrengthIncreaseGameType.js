var abilityBasedGameType = require('./abilityBasedGameType');



exports.addPlanetAbilities = function(planetsFarFromHomes){

	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,[abilityBasedGameType.DEF_INC_ABILITY]);
	
}

exports.findCorrectDefenseForAPlanet = function(config, planets, player, game){	
	var defMultiplier = 0.0;
	var abilityDetected = false;
	
	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if(hasTheDefenceAbility(planet) && hasTheSameOwner(planet, player)){
			defMultiplier = defMultiplier + parseFloat(config.values['defenseModifier']);
			abilityDetected = true;
		}
	}
	
	if(abilityDetected){
		defMultiplier += abilityBasedGameType.harvestEnhancement(player, game, abilityBasedGameType.DEF_INC_ABILITY)
	}
	
	return defMultiplier;
	
}

var hasTheDefenceAbility = function(planet){
	return planet.ability && planet.ability === abilityBasedGameType.DEF_INC_ABILITY;
}

var hasTheSameOwner = function(planet, player){
	return player === planet.handle;
}



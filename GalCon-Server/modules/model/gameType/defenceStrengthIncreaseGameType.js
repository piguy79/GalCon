var abilityBasedGameType = require('./abilityBasedGameType');

var DEF_INC_ABILITY = 'defenseModifier';


exports.addPlanetAbilities = function(planetsFarFromHomes){

	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,DEF_INC_ABILITY);
	
}

exports.findCorrectDefenseForAPlanet = function(config, planets, player){	
	var defMultiplier = 0.0;
	
	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if(hasTheDefenceAbility(planet) && hasTheSameOwner(planet, player)){
			defMultiplier = defMultiplier + parseFloat(config.values['defenseModifier']);
		}
	}
	
	return defMultiplier / 100.0;
	
}

var hasTheDefenceAbility = function(planet){
	return planet.ability && planet.ability === DEF_INC_ABILITY;
}

var hasTheSameOwner = function(planet, player){
	return player === planet.ownerHandle;
}



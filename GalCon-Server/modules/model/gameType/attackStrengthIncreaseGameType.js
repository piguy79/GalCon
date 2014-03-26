var abilityBasedGameType = require('./abilityBasedGameType');




exports.addPlanetAbilities = function(planetsFarFromHomes){

	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,[abilityBasedGameType.ATTACK_INC_ABILITY]);
	
}

exports.findCorrectFleetToAttackEnemyPlanet = function(config, planets, player){

	var attackMultiplier = 0.0;

	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if((planet.ability && planet.ability == abilityBasedGameType.ATTACK_INC_ABILITY) && planet.ownerHandle == player){
			attackMultiplier = attackMultiplier + parseFloat(config.values[abilityBasedGameType.ATTACK_INC_ABILITY]);
		}
	}
	
	return attackMultiplier / 100.0;
	
}



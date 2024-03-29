var abilityBasedGameType = require('./abilityBasedGameType');




exports.addPlanetAbilities = function(planetsFarFromHomes){

	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,[abilityBasedGameType.ATTACK_INC_ABILITY]);
	
}

exports.findCorrectFleetToAttackEnemyPlanet = function(config, planets, player, game){

	var attackMultiplier = 0.0;
	var abilityDetected = false;

	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if(planet.status === "DEAD") {
			continue;
		}
		
		if((planet.ability && planet.ability == abilityBasedGameType.ATTACK_INC_ABILITY) && planet.handle == player){
			attackMultiplier = attackMultiplier + parseFloat(config.values[abilityBasedGameType.ATTACK_INC_ABILITY]);
			abilityDetected = true;
		}
	}
	
	if(abilityDetected){
		attackMultiplier += abilityBasedGameType.harvestEnhancement(player, game, abilityBasedGameType.ATTACK_INC_ABILITY)
	}
	
	return attackMultiplier;
	
}



var abilityBasedGameType = require('./abilityBasedGameType');

var ATTACK_INC_ABILITY = 'attackModifier';


exports.addPlanetAbilities = function(planetsFarFromHomes){

	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,ATTACK_INC_ABILITY);
	
}

exports.findCorrectFleetToAttackEnemyPlanet = function(config, planets, player){

	var attackMultiplier = 0.0;

	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if((planet.ability && planet.ability == ATTACK_INC_ABILITY) && planet.ownerHandle == player){
			attackMultiplier = attackMultiplier + parseFloat(config.values[ATTACK_INC_ABILITY]);
		}
	}
	
	return attackMultiplier / 100.0;
	
}



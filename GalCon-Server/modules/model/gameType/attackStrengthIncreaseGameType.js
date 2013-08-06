var abilityBasedGameType = require('./abilityBasedGameType');

var ATTACK_INC_ABIBILITY = 'ATTACK_INC';


exports.addPlanetAbilities = function(planetsFarFromHomes){

	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,ATTACK_INC_ABIBILITY);
	
}

exports.findCorrectFleetToAttackEnemyPlanet = function(config, planets, player){

	var attackMultiplier = 0.0;

	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if((planet.ability && planet.ability == ATTACK_INC_ABIBILITY) && planet.ownerHandle == player){
			attackMultiplier = attackMultiplier + parseFloat(config.values['attackModifier']);
		}
	}
	
	return attackMultiplier / 100.0;
	
}



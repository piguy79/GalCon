var abilityBasedGameType = require('./abilityBasedGameType');

var ATTACK_BOOST = 0.5;
var ATTACK_INC_ABIBILITY = 'ATTACK_INC';


exports.addPlanetAbilities = function(planetsFarFromHomes){

	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,ATTACK_INC_ABIBILITY);
	
}

exports.findCorrectFleetToAttackEnemyPlanet = function(planets, player, currentFleet){

	var attackMultiplier = 0;

	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if((planet.ability && planet.ability == ATTACK_INC_ABIBILITY) && planet.owner == player){
			attackMultiplier = attackMultiplier + ATTACK_BOOST;
		}
	}
	
	return currentFleet + (currentFleet * attackMultiplier);
	
}



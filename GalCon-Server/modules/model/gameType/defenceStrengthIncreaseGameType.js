var abilityBasedGameType = require('./abilityBasedGameType');

var DEF_BOOST = 0.5;
var DEF_INC_ABIBILITY = 'DEF_INC';


exports.addPlanetAbilities = function(planetsFarFromHomes){

	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,DEF_INC_ABIBILITY);
	
}

exports.findCorrectDefenseForAPlanet = function(planets, attackedPlanet){

	var defMultiplier = 0;

	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if(hasTheDefenceAbility(planet) && hasTheSameOwnerAsTheAttackedPlanet(planet, attackedPlanet)){
			defMultiplier = defMultiplier + DEF_BOOST;
		}
	}
	
	return defMultiplier;
	
}

var hasTheDefenceAbility = function(planet){
	return planet.ability && planet.ability == DEF_INC_ABIBILITY;
}

var hasTheSameOwnerAsTheAttackedPlanet = function(planet, attackedPlanet){
	return planet.ownerHandle && planet.ownerHandle == attackedPlanet.ownerHandle;
}



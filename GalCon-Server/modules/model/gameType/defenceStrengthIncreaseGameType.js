var abilityBasedGameType = require('./abilityBasedGameType');

var DEF_BOOST = 0.5;
var DEF_INC_ABIBILITY = 'DEF_INC';


exports.addPlanetAbilities = function(planetsFarFromHomes){

	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,DEF_INC_ABIBILITY);
	
}

exports.findCorrectDefenseForAPlanet = function(planets, player){

	var defMultiplier = 0;

	for(var  i = 0; i < planets.length; i++){
		var planet = planets[i];
		if(hasTheDefenceAbility(planet) && hasTheSameOwner(planet, player)){
			defMultiplier = defMultiplier + DEF_BOOST;
		}
	}
	
	return defMultiplier;
	
}

var hasTheDefenceAbility = function(planet){
	return planet.ability && planet.ability == DEF_INC_ABIBILITY;
}

var hasTheSameOwner = function(player, planet){
	return player == planet.ownerHandle;
}



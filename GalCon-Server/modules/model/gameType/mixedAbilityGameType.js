var abilityBasedGameType = require('./abilityBasedGameType');

exports.addPlanetAbilities = function(planetsFarFromHomes){
	var allAbilities = [abilityBasedGameType.ATTACK_INC_ABILITY, abilityBasedGameType.DEF_INC_ABILITY, abilityBasedGameType.SPEED_ABILITY];
	abilityBasedGameType.addPlanetAbilities(planetsFarFromHomes,allAbilities);
}
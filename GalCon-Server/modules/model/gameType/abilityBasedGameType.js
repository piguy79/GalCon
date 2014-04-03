var SHIP_REGEN_RATE = 1;
var SHIP_NUMBER = 10;

exports.DEF_INC_ABILITY = 'defenseModifier';
exports.ATTACK_INC_ABILITY = 'attackModifier';
exports.SPEED_ABILITY = 'speedModifier';


exports.addPlanetAbilities = function(planetsFarFromHomes, abilitiesArray){

	var abilitiesToAdd = planetsFarFromHomes.length * 0.2;
	
	console.log("ABILITIES ARRAY **** " + abilitiesArray);
	console.log(abilitiesArray[1]);
	
	if(abilitiesToAdd < 1){
		abilitiesToAdd = 1;
	}
	var addedAbilities = 0;

	for(var i=0; i < planetsFarFromHomes.length; i++){
		var planetToAddTo = planetsFarFromHomes[i];
		
		if(!planetToAddTo.ability){
			planetToAddTo.regen = SHIP_REGEN_RATE;
			planetToAddTo.ships = SHIP_NUMBER;
			
			var abilityIndex = Math.floor(Math.random() * (abilitiesArray.length));
			planetToAddTo.ability = abilitiesArray[abilityIndex];
			addedAbilities++;
			
			if(addedAbilities >= abilitiesToAdd){
				break;
			}
		}
	}
	
	
}

exports.harvestEnhancement = function(player, game){
	
	var harvestEnhance = 0;	
	var harvestCapablePlanets = _.filter(game.planets, function(planet){ return planet.handle === player && planet.ability && planet.harvest && planet.harvest.status === "ACTIVE"});
	
	if(harvestCapablePlanets.length > 0){
		harvestEnhance = parseFloat(game.config.values['harvestEnhancement']) * harvestCapablePlanets.length;
	}
	
	return harvestEnhance;	
}



var SHIP_REGEN_RATE = 1;
var SHIP_NUMBER = 10;


exports.addPlanetAbilities = function(planetsFarFromHomes, abilityToAdd){

	var abilitiesToAdd = planetsFarFromHomes.length * 0.2;
	
	if(abilitiesToAdd < 1){
		abilitiesToAdd = 1;
	}
	var addedAbilities = 0;

	for(var i=0; i < planetsFarFromHomes.length; i++){
		var planetToAddTo = planetsFarFromHomes[i];
		
		if(!planetToAddTo.ability){
			planetToAddTo.shipRegenRate = SHIP_REGEN_RATE;
			planetToAddTo.numberOfShips = SHIP_NUMBER;
			planetToAddTo.ability = abilityToAdd;
			addedAbilities++;
			
			if(addedAbilities >= abilitiesToAdd){
				break;
			}
		}
	}
	
	
}

exports.harvestEnhancement = function(player, game){
	
	var harvestEnhance = 0;	
	var harvestCapablePlanets = _.filter(game.planets, function(planet){ return planet.ownerHandle === player && planet.ability && planet.harvest && planet.harvest.status === "ACTIVE"});
	
	if(harvestCapablePlanets.length > 0){
		harvestEnhance = parseFloat(game.config.values['harvestEnhancement']) * harvestCapablePlanets.length;
	}
	
	return harvestEnhance;	
}



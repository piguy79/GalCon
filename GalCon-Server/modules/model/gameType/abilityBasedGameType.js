var SHIP_REGEN_RATE = 1;
var SHIP_NUMBER = 10;


exports.addPlanetAbilities = function(planetsFarFromHomes, abilityToAdd){

	for(var i=0; i < planetsFarFromHomes.length; i++){
		var planetToAddTo = planetsFarFromHomes[i];
		
		if(!planetToAddTo.ability){
			planetToAddTo.shipRegenRate = SHIP_REGEN_RATE;
			planetToAddTo.numberOfShips = SHIP_NUMBER;
			planetToAddTo.ability = abilityToAdd;
			break;
		}
	}	
}



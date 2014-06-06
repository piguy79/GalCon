var galconMath = require('./math/galconMath'),
_ = require('underscore'),
gameTypeAssembler = require('./model/gameType/gameTypeAssembler');

var STARTING_SCORE = 0;

exports.createAiMoves = function(game, map){
	var planetsPartition = _.partition(game.planets, function(planet){
		return planet.handle === 'AI';
	});
	
	var planetsOwnedByAi = planetsPartition[0];
	var planetsNotOwnedByAi = planetsPartition[1];
	
	var movesToMake = [];
	var aggressiveRound = Math.random() > map.aiConfig.aggressiveThreshold;
	
	_.each(planetsOwnedByAi, function(ownedPlanet){
		
		if(ownedPlanet.harvest.status !== 'ACTIVE'){
			var scoreByPlanet = _.map(planetsNotOwnedByAi, function(otherPlanet){
				var score = STARTING_SCORE;
				var distance = galconMath.distance(ownedPlanet.pos, otherPlanet.pos);
				score = score - (distance * map.aiConfig.distanceEffect);
				score = score + (otherPlanet.regen * map.aiConfig.regenEffect);
				score = score - (otherPlanet.ships * map.aiConfig.shipCountEffect);
				if(aggressiveRound && otherPlanet.handle !== '' && ((planetsOwnedByAi.length / game.planets.length) > 0.5)){
					score = score + (Math.abs(score) * map.aiConfig.agressionEffect);
				}
				if(otherPlanet.ability && otherPlanet.ability !== ''){
					score = score + (Math.abs(score) * map.aiConfig.abilityPlanetEffect);
				}
				
				return {planet : otherPlanet, score : Math.round(score)};
			});
			
			var availableShips = Math.round(ownedPlanet.ships * map.aiConfig.shipsAvailable);
			_.each(_.sortBy(scoreByPlanet, function(item){
				return item.score * -1;
			}), function(planetScore){
				if(availableShips > 0){
					var maxSend = planetScore.planet.ships === 0 ? 1 : Math.round(planetScore.planet.ships * map.aiConfig.maxSendEffect);
					if((availableShips - maxSend) > 0 ){
						availableShips = availableShips - maxSend;
						movesToMake.push(createMove(ownedPlanet, planetScore.planet, maxSend));
					} else if(availableShips > 0){
						movesToMake.push(createMove(ownedPlanet, planetScore.planet, availableShips));
						availableShips = 0;
					}
					
				}
			});
		}
		
	});
		
	return _.filter(movesToMake, function(move){
		return move.fleet !== 0;
	});
}

exports.createHarvest = function(game, map){
	var abilityPlanetsOwnedByAi = _.filter(game.planets, function(planet){
		return planet.handle === 'AI' && planet.ability && planet.ability !== '' && planet.harvest.status !== "ACTIVE" && planet.status !== 'DEAD';
	});
		
	var harvestMoves = [];
	
	_.each(abilityPlanetsOwnedByAi, function(planet){
		if(gameTypeAssembler.gameTypes[game.gameType].harvestAvailable && Math.random() > 0.6){
			harvestMoves.push({planet : planet.name});
		}
	});
	
	
	return harvestMoves;
}


var createMove = function(fromPlanet, toPlanet, shipsToSend){
	return {
		handle : 'AI',
		from : fromPlanet.name,
		to : toPlanet.name,
		fleet : shipsToSend,
		curPos : fromPlanet.pos,
		prevPos : fromPlanet.pos,
		executed : false,
		duration : galconMath.distance(fromPlanet.pos, toPlanet.pos)
	}
}
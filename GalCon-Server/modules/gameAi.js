var galconMath = require('./math/galconMath'),
_ = require('underscore');

var STARTING_SCORE = 0;
var DISTANCE_EFFECT = 1.3;
var REGEN_EFFECT = 2;
var SHIP_COUNT_EFFECT = 0.5;
var SHIPS_AVAILABLE = 0.8;
var AGGRESION_EFFECT = 3;
var ABILITY_PLANET_EFFECT = 2;

exports.createAiMoves = function(game){
	
	var planetsPartition = _.partition(game.planets, function(planet){
		return planet.handle === 'AI';
	});
	
	var planetsOwnedByAi = planetsPartition[0];
	var planetsNotOwnedByAi = planetsPartition[1];
	
	var movesToMake = [];
	var aggressiveRound = Math.random() > 0.5;
	
	_.each(planetsOwnedByAi, function(ownedPlanet){
		var scoreByPlanet = _.map(planetsNotOwnedByAi, function(otherPlanet){
			var score = STARTING_SCORE;
			var distance = galconMath.distance(ownedPlanet.pos, otherPlanet.pos);
			score = score - (distance * DISTANCE_EFFECT);
			score = score + (otherPlanet.regen * REGEN_EFFECT);
			score = score - (otherPlanet.ships * SHIP_COUNT_EFFECT);
			if(aggressiveRound && otherPlanet.handle !== ''){
				score = score * AGGRESION_EFFECT;
			}
			if(otherPlanet.ability !== ''){
				score = score * ABILITY_PLANET_EFFECT;
			}
			
			return {planet : otherPlanet, score : Math.round(score)};
		});
		
		var availableShips = Math.round(ownedPlanet.ships * SHIPS_AVAILABLE);
		_.each(_.sortBy(scoreByPlanet, function(item){
			return item.score * -1;
		}), function(planetScore){
			if(availableShips > 0){
				var maxSend = Math.round(planetScore.planet.ships * 1.5);
				if((availableShips - maxSend) > 0 ){
					availableShips = availableShips - maxSend;
					movesToMake.push(createMove(ownedPlanet, planetScore.planet, maxSend));
				} else if(availableShips > 0){
					movesToMake.push(createMove(ownedPlanet, planetScore.planet, availableShips));
					availableShips = 0;
				}
				
			}
		});
	});
		
	return movesToMake;
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
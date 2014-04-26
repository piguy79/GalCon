var validator = require("validator"),
	_ = require("underscore"),
	mongoose = require('./model/mongooseConnection').mongoose,
	gameManager = require('./model/game');

exports.validate = function(gameId, handle, moves, harvests){
	
	var promise = new mongoose.Promise();
	var p = gameManager.findById(gameId);
	p.then(function(game) {
		var valid = false;
		if(moves && moves.length > 0){
			valid = runValidate(game, handle, moves, harvests);
		}else{
			valid = runPlayerValidate(game, handle);
		}
		promise.fulfill({success : valid});
		
	}).then(null, function(err){promise.fulfill({success : false});});
	
	return promise;

}

var runValidate = function(game, handle, moves, harvests){
	return playerOwnsFromPlanets(game, handle, moves) && fleetsCannotExceedTheshipsOnAPlanet(game.planets, moves) 
	&& mustBeValidFromAndToPlanets(game.planets, moves) && playerHasNotMovedThisRound(game, handle) && gameIsNotOver(game) && playerIsPartOfThisGame(game, handle)
	&& harvestIsValid(game, harvests);
}

var runPlayerValidate = function(game, handle){
	return playerHasNotMovedThisRound(game, handle) && gameIsNotOver(game) && playerIsPartOfThisGame(game, handle);
}

var playerOwnsFromPlanets = function(game, handle, moves){
	var moveFromPlanets = _.pluck(moves, 'from');
	var ownedPlanets = _.filter(game.planets, function(planet){
		return planet.handle === handle;
	});
	
	return _.difference(moveFromPlanets, _.pluck(ownedPlanets, 'name')).length === 0;
}

var fleetsCannotExceedTheshipsOnAPlanet = function(planets , moves){
	var moveFromPlanets = _.groupBy(moves, 'from');
	var planetsInvolvedInMove = _.filter(planets, function(planet){
		return moveFromPlanets[planet.name];
	});
	
	var result = true;
	
	_.each(planetsInvolvedInMove, function(planet){
		var fleetMovesForThisPlanet = _.reduce(moveFromPlanets[planet.name], function(memo, move){ return parseInt(memo) + parseInt(move.fleet);}, 0);
		if(planet.ships < fleetMovesForThisPlanet){
			result = false;
		}
	});
	
	return result;
}

var mustBeValidFromAndToPlanets = function(planets, moves){
	var planetNames = _.pluck(planets, 'name');
	var result = true;
	
	_.each(moves, function(move){
		if(!_.contains(planetNames, move.from) || !_.contains(planetNames, move.to)){
			result = false;
		}
	});
	
	return result;
	
}

var playerHasNotMovedThisRound = function(game, handle){
	return !_.contains(game.round.moved, handle);
}

var gameIsNotOver = function(game){
	if(game.endGame.winnerHandle){
		return false;
	};
	
	return true;
}

var playerIsPartOfThisGame = function(game, handle){
	var movePlayer = _.filter(game.players, function(player){
		return player.handle === handle;
	});
		
	return movePlayer && movePlayer !== "" ? true : false;
}

var harvestIsValid = function(game, handle, harvests){
	if(harvests && harvests.length > 0){
		return harvestIsAvailableForThisGameType(game) && playerOwnsHarvestPlanets(game.planets, handle, harvests);
	}
	
	return true;
}

var harvestIsAvailableForThisGameType = function(game){
	if(gameTypeAssembler.gameTypes[game.gameType].harvestAvailable && gameTypeAssembler.gameTypes[game.gameType].harvestAvailable === true){
		return true;
	}
	
	return false;
}

var playerOwnsHarvestPlanets = function(planets, handle, harvests){
	var harvestPlanets = _.pluck(harvests, 'planet');
	var ownedPlanets = _.filter(game.planets, function(planet){
		return planet.handle === handle;
	});
	
	return _.difference(harvestPlanets, _.pluck(ownedPlanets, 'name')).length === 0;
}

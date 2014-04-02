var validator = require("validator"),
	_ = require("underscore"),
	mongoose = require('./model/mongooseConnection').mongoose,
	gameManager = require('./model/game');

exports.validate = function(gameId, handle, moves){
	
	var promise = new mongoose.Promise();
	var p = gameManager.findById(gameId);
	p.then(function(game) {
		var valid = false;
		if(moves && moves.length > 0){
			valid = runValidate(game, handle, moves);
		}else{
			valid = runPlayerValidate(game, handle);
		}
		promise.complete({success : valid});
		
	}).then(null, function(err){promise.complete({success : false});});
	
	return promise;

}

var runValidate = function(game, handle, moves){
	return playerOwnsFromPlanets(game, handle, moves) && fleetsCannotExceedTheNumberOfShipsOnAPlanet(game.planets, moves) 
	&& mustBeValidFromAndToPlanets(game.planets, moves) && playerHasNotMovedThisRound(game, handle) && gameIsNotOver(game) && playerIsPartOfThisGame(game, handle);
}

var runPlayerValidate = function(game, handle){
	return playerHasNotMovedThisRound(game, handle) && gameIsNotOver(game) && playerIsPartOfThisGame(game, handle);
}

var playerOwnsFromPlanets = function(game, handle, moves){
	var moveFromPlanets = _.pluck(moves, 'fromPlanet');
	var ownedPlanets = _.filter(game.planets, function(planet){
		return planet.ownerHandle === handle;
	});
	
	return _.difference(moveFromPlanets, _.pluck(ownedPlanets, 'name')).length === 0;
}

var fleetsCannotExceedTheNumberOfShipsOnAPlanet = function(planets , moves){
	var moveFromPlanets = _.groupBy(moves, 'fromPlanet');
	var planetsInvolvedInMove = _.filter(planets, function(planet){
		return moveFromPlanets[planet.name];
	});
	
	var result = true;
	
	_.each(planetsInvolvedInMove, function(planet){
		var fleetMovesForThisPlanet = _.reduce(moveFromPlanets[planet.name], function(memo, move){ return parseInt(memo) + parseInt(move.fleet);}, 0);
		if(planet.numberOfShips < fleetMovesForThisPlanet){
			result = false;
		}
	});
	
	return result;
}

var mustBeValidFromAndToPlanets = function(planets, moves){
	var planetNames = _.pluck(planets, 'name');
	var result = true;
	
	_.each(moves, function(move){
		if(!_.contains(planetNames, move.fromPlanet) || !_.contains(planetNames, move.toPlanet)){
			result = false;
		}
	});
	
	return result;
	
}

var playerHasNotMovedThisRound = function(game, handle){
	return !_.contains(game.currentRound.playersWhoMoved, handle);
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

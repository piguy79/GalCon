var validator = require("validator"),
	_ = require("underscore"),
	mongoose = require('./model/mongooseConnection').mongoose,
	gameManager = require('./model/game');

exports.validate = function(gameId, handle){
	
	var promise = new mongoose.Promise();
	var p = gameManager.findById(gameId);
	p.then(function(game) {
		var valid = runValidate(game, handle);
		promise.complete({success : valid});
		
	}).then(null, function(err){promise.complete({success : false});});
	
	return promise;

}

exports.validateClaim = function(game, handle){
	return runValidate(game, handle);
}

var runValidate = function(game, handle){
	return playerIsPartOfThisGame(game, handle) && gameHasTwoPlayers(game) && claimIsAvailable(game) && playerMovedThisRound(game, handle);
}

var playerIsPartOfThisGame = function(game, handle){
	var matchingPlayer = _.filter(game.players, function(player){
		return player.handle === handle;
	});
	console.log('matchingPlayer.length : ' + matchingPlayer.length)
	return matchingPlayer.length > 0;
}

var gameHasTwoPlayers = function(game){
	console.log('game.players.length ' + game.players.length);
	return game.players.length === 2;
}

var claimIsAvailable = function(game){
	var timeout = game.config.values['claimTimeout'];
	console.log('timeout ' + timeout);
	var diff = Date.now() - game.moveTime;
	console.log('now : ' + Date.now());
	console.log('game.moveTime: ' + game.moveTime);
	console.log('diff ' + diff);
	
	console.log('diff >= timeout ' + (diff >= timeout));
	return diff >= timeout;
}

var playerMovedThisRound = function(game, handle){
	console.log('game.round.moved ' + game.round.moved);
	return _.contains(game.round.moved, handle);
}


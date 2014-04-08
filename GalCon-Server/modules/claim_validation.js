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

var runValidate = function(game, handle){
	return playerIsPartOfThisGame(game, handle) && gameHasTwoPlayers(game) && claimIsAvailable(game);
}

var playerIsPartOfThisGame = function(game, handle){
	var matchingPlayer = _.filter(game.players, function(player){
		return player.handle === handle;
	});
	
	return matchingPlayer.length > 0;
}

var gameHasTwoPlayers = function(game){
	return game.players.length === 2;
}

var claimIsAvailable = function(game){
	var timeout = game.config.values['claimTimeout'];
	var diff = Date.now() - game.moveTime;
	
	return diff >= timeout;
}




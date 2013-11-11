var apiRunner = require('../fixtures/apiRunner'), 
	mongoose = require('mongoose');

exports.createGameForPlayers = function(player1, player2, map){
	var p = new mongoose.Promise();  		
	var runnerPromise = apiRunner.matchPlayerToGame(player1, map);
	runnerPromise.then(function(game){
		return apiRunner.matchPlayerToGame(player2, map);
	}).then(function(game){
		p.complete(game);
	}, function(err){
		p.reject(err);
	});
	return p;
}
	
exports.performTurn = function(currentGameId, player1, player2){
	var p = new mongoose.Promise();
	var movePromise = apiRunner.performMove(currentGameId, player1.moves, player1.handle, player1.time, player1.harvest);
	movePromise.then(function(game){
		return apiRunner.performMove(currentGameId, player2.moves, player2.handle, player2.time, player2.harvest); 
	}).then(function(game){
		p.complete(game);
	}, function(err){
		p.reject(err);
	});
	return p;
}

exports.performTurns = function(turnCount, currentGameId, player1, player2){
	var promise = new mongoose.Promise();
	promise.complete();
	var lastPromise = promise;
	
	for(var i = 0; i < turnCount; i++){
		lastPromise = lastPromise.then(function(){
			return exports.performTurn(currentGameId, player1, player2);
		});
	}
	
	return lastPromise;
	
}
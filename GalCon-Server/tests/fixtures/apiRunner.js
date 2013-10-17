var needle = require("needle"), mongoose = require('mongoose');

var localhost = "http://localhost:3000";

var needleWithPromise = function(func, url, postBody) {
	var p = new mongoose.Promise();

	var args = [ localhost + url ];
	if (postBody) args.push(postBody);
	args.push(function(err, res, body) {
		if (err) p.reject(err);
		else p.complete(body);
	});

	func.apply(needle, args);

	return p;
}

exports.findGame = function(gameId) {
	return needleWithPromise(needle.get, "/findGameById?id=" + gameId);
}

exports.matchPlayerToGame = function(playerHandle, mapKey) {
	var postData = {
		mapToFind : mapKey,
		playerHandle : playerHandle,
		time : new Date().getTime()
	};

	return needleWithPromise(needle.post, "/matchPlayerToGame", postData);
};

exports.performMove = function(gameId, moves, player, callBack) {
	var postData = {
		moves : moves,
		id : gameId,
		player : player
	}

	return needleWithPromise(needle.post, "/performMoves", postData);
}

exports.joinGame = function(gameId, playerToJoin, callback) {
	return needleWithPromise(needle.get, "/joinGame?id=" + gameId + "&player=" + playerToJoin);
}

exports.findCurrentGamesByUserName = function(userName, callback) {
	return needleWithPromise(needle.get, "/findActiveGamesForUser?userName=" + userName);
}

exports.findAvailableGamesForUser = function(player, callback) {
	return needleWithPromise(needle.get, "/findAvailableGames?player=" + player);
}
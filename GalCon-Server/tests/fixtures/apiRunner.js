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

exports.performMove = function(gameId, moves, playerHandle, time, harvest) {
	var postData = {
		moves : moves,
		id : gameId,
		playerHandle : playerHandle,
		time : time || 1000,
		harvest : harvest || []
	}

	return needleWithPromise(needle.post, "/performMoves", postData);
}

exports.joinGame = function(gameId, playerToJoin) {
	return needleWithPromise(needle.get, "/joinGame?id=" + gameId + "&playerHandle=" + playerToJoin);
}

exports.findCurrentGamesByPlayerHandle = function(playerHandle) {
	return needleWithPromise(needle.get, "/findCurrentGamesByPlayerHandle?playerHandle=" + playerHandle);
}

exports.findAvailableGames = function(playerHandle) {
	return needleWithPromise(needle.get, "/findAvailableGames?playerHandle=" + playerHandle);
}

exports.addCoinsForAnOrder = function(playerHandle, orders){
	var postData = {
			playerHandle : playerHandle,
			orders : orders
	};
	
	return needleWithPromise(needle.post, '/addCoinsForAnOrder', postData);
}

exports.deleteConsumedOrders = function(playerHandle, orders){
	var postData = {
			playerHandle : playerHandle,
			orders : orders
	};
	
	return needleWithPromise(needle.post, '/deleteConsumedOrders', postData);
}

exports.updateUserCoinsInformation = function(playerHandle, time){
	var postData = {
		playerHandle : playerHandle,
		time : time
	};
	return needleWithPromise(needle.post, '/updateUserCoinsInformation', postData);
}

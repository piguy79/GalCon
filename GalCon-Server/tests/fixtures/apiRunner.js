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

exports.matchPlayerToGame = function(playerHandle, mapKey, session) {
	var postData = {
		mapToFind : mapKey,
		handle : playerHandle,
		session : session
	};

	return needleWithPromise(needle.post, "/matchPlayerToGame", postData);
};

exports.performMove = function(gameId, moves, playerHandle, time, harvest) {
	var postData = {
		moves : moves,
		id : gameId,
		playerHandle : playerHandle,
		time : time || 1000,
		harvest : harvest || [],
		session : "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592"
	}

	return needleWithPromise(needle.post, "/performMoves", postData);
}

exports.joinGame = function(gameId, playerToJoin) {
	return needleWithPromise(needle.get, "/joinGame?id=" + gameId + "&playerHandle=" + playerToJoin);
}

exports.findCurrentGamesByPlayerHandle = function(handle, session) {
	return needleWithPromise(needle.get, "/findCurrentGamesByPlayerHandle?handle=" + handle + "&session=" + session);
}

exports.findGamesWithPendingMove = function(handle) {
	return needleWithPromise(needle.get, "/findGamesWithPendingMove?handle=" + handle);
}

exports.findAvailableGames = function(playerHandle) {
	return needleWithPromise(needle.get, "/findAvailableGames?playerHandle=" + playerHandle);
}

exports.addFreeCoins = function(handle, session){
	var postData = {
		handle : handle,
		session : session
	};
	
	return needleWithPromise(needle.post, '/addFreeCoins', postData);
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

exports.exchangeToken = function(authProvider, token) {
	var postData = {
		authProvider : authProvider,
		token : token
	};
	return needleWithPromise(needle.post, '/sessions/exchangeToken', postData);
}

exports.requestHandleForEmail = function(session, email, handle) {
	var postData = {
		session : session,
		email : email,
		handle : handle
	};
	return needleWithPromise(needle.post, '/requestHandleForEmail', postData);
}

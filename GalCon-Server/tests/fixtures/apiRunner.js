var needle = require("needle")

var localhost = "http://localhost:3000";

exports.performMove = function(gameId, moves, player, callBack) {
	var postData = {
		moves : moves,
		id : gameId,
		player : player
	}

	needle.post(localhost + "/performMoves", postData, function(err, response, body) {
		callBack();
	});
}

exports.findGame = function(gameId, callback) {
	needle.get(localhost + "/findGameById?id=" + gameId, function(err, response, body) {
		callback(body);
	});
}

exports.generateGame = function(player, callback) {
	var postData = {
		width : 8,
		height : 15,
		player : player
	}

	needle.post(localhost + "/generateGame", postData, function(err, response, body) {
		callback(body);
	});
};

exports.deleteGame = function(gameId, callback) {
	needle.get(localhost + "/deleteGame?id=" + gameId, function(err, response, body) {
		callback(body);
	});
}

exports.addPlanets = function(gameId, planetsForTest, callback) {
	var postData = {
		id : gameId,
		planets : planetsForTest
	};

	needle.post(localhost + "/addPlanetsToGame", postData, function(err, response, body) {
		callback();
	});
}

exports.joinGame = function(gameId, playerToJoin, callback) {
	needle.get(localhost + "/joinGame?id=" + gameId + "&player=" + playerToJoin, function(err, response, body) {
		callback();
	});
}

exports.findCurrentGamesByUserName = function(userName, callback) {
	needle.get(localhost + "/findActiveGamesForUser?userName=" + userName, function(err, response, body) {
		callback(body);
	});
}

exports.findAvailableGamesForUser = function(player, callback) {
	needle.get(localhost + "/findAvailableGames?player=" + player, function(err, response, body) {
		callback(body.items);
	});
}
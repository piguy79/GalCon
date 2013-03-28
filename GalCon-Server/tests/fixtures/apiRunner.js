var needle = require("needle")

exports.performMove = function(gameId, moves, player, callBack){

	var postData = {
		moves : moves,
		id : gameId,
		player : player
	}
	
	needle.post("http://localhost:3000/performMoves",postData , function(err, response, body){
			callBack();
	});
}


exports.findGame = function(gameId, callback){
	needle.get("http://localhost:3000/findGameById?id=" + gameId, function(err, response, body){
		callback(body);
	});
		
}

exports.generateGame = function(callback){
	needle.post("http://localhost:3000/generateGame","player=moveTest", function(err, response, body){
			callback(body);
		});
};

exports.deleteGame = function(gameId, callback){
	needle.get("http://localhost:3000/deleteGame?id=" + gameId, function(err, response, body){
		callback(body);
	});
		
}

exports.addPlanets = function(gameId, planetsForTest, callback){
	var postData = {
		id : gameId,
		planets : planetsForTest
	};
		
	needle.post("http://localhost:3000/addPlanetsToGame",postData, function(err, response, body){
		callback();
	});
		
}
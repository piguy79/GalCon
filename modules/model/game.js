var mongoose = require('mongoose')
, db = mongoose.createConnection('localhost', 'galcon')
gamebuilder = require('../gameBuilder');

var schema = mongoose.Schema({
	players : [String],
	createdDate : "Date",
	currentRound : {
		roundNumber : "Number",
		player : "Number"
	},
	numberOfPlanets : "Number",
	planets : [
		{
			name : "String",
			owner : String,
			position : {
				x : "Number",
				y : "Number"
			},
			shipRegenRate : "Number",
			numberOfShips : "Number"
		}
	]
});

var GameModel = db.model('Game', schema);

exports.createGame = function(players, numberOfPlanets, callback){

	var game = gamebuilder.createGameBuilder(players, 10);
	game.createBoard(function(createdGame){
		var constructedGame = new GameModel(createdGame);
		callback(constructedGame);
	});
	

	
}

exports.findById = function(gameId, callback){
	GameModel.find({id : gameId}, function(err, games){
		if(err){
			console.log("Unable to find games");
		}else{
			console.log("Found games");
			callback(games);
		}
	});
}

exports.saveGame = function(game, callback){
	game.save(function(err){
		if(err){
			console.log("Something went wrong");
		}else{
			console.log("Game Saved with ID : " + game.id);
			callback();
		}

	});
}


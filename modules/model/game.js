var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
, ObjectId = require('mongoose').Types.ObjectId; 
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
			owner : "String",
			position : {
				x : "Number",
				y : "Number"
			},
			shipRegenRate : "Number",
			numberOfShips : "Number"
		}
	]
});

schema.set('toObject', { getters: true });
var GameModel = db.model('Game', schema);

exports.createGame = function(players, numberOfPlanets, callback){

	var game = gamebuilder.createGameBuilder(players, 10);
	game.createBoard(function(createdGame){
		var constructedGame = new GameModel(createdGame);
		callback(constructedGame);
	});
	

	
};

exports.findAllGames = function(callback){
	console.log("Call Database");
	GameModel.find({}, function(err, games){
		if(err){
			console.log("Unable to find games");
		}else{
			console.log("Found games");
			callback(games);
		}
	});
};

exports.findById = function(gameId, callback){
	GameModel.findById(gameId, function(err, game){
		if(err){
			console.log("Unable to find games");
		}else{
			callback(game);
		}
	});
};

exports.findAvailableGames = function(callback){
	GameModel.find({players : {$size : 1}}, function(err, games){
		if(err){
			next();
		}else{
			callback(games);
		}
	});
};


exports.saveGame = function(game, callback){
	game.save(function(err){
		if(err){
			console.log("Something went wrong. " + err);
		}else{
			console.log("Game Saved with ID : " + game.id);
			callback();
		}

	});
}

exports.addUser = function(gameId, player, callback){
	this.findById(gameId, function(game){
		game.players.push(player);
		var assigned = false;

		while(!assigned){
			var randomPlanetIndex = Math.floor((Math.random()*game.planets.length));
			if(typeof game.planets[randomPlanetIndex].owner === "undefined"){
				game.planets[randomPlanetIndex].owner = player;
				assigned = true;
			}
		}
		game.save(function(savedGame){
			callback(savedGame);
		});
	});
}


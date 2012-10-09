var mongoose = require('mongoose')
, mongoUrl = process.env.MONGO_URL || 'mongodb://localhost:27017/galcon'
, db = mongoose.connect(mongoUrl)
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
	GameModel.update({_id : new ObjectId(gameId)},
				{$push : {players : player}}, function(){
					callback();
				})
}


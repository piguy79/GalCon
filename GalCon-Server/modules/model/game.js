var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
, ObjectId = require('mongoose').Types.ObjectId; 
gamebuilder = require('../gameBuilder');

var gameSchema = mongoose.Schema({
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
	],
	moves : [
		{
			player : "String",
			fromPlanet : "String",
			toPlanet : "String",
			fleet : "Number",
			duration : "Number"
		}
	]
	
});

gameSchema.set('toObject', { getters: true });



var GameModel = db.model('Game', gameSchema);

exports.createGame = function(players, numberOfPlanets, callback){

	var game = gamebuilder.createGameBuilder(players, 10);
	game.createBoard(function(createdGame){
		var constructedGame = new GameModel(createdGame);
		callback(constructedGame);
	});
	

	
};

exports.findAllGames = function(callback){
	GameModel.find({}, function(err, games){
		if(err){
			console.log("Unable to find games");
		}else{
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

exports.deleteGame = function(gameId, callback){
	GameModel.findById(gameId).remove();
	callback();

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
			callback();
		}

	});
}

exports.performMoves = function(gameId, moves, player, callback){

	this.findById(gameId, function(game){
	if(game.moves){	
	
		var movesToRemove = [];
		for(var i = 0 ; i < game.moves.length; i++){
			var move = game.moves[i];
			if(move.duration == 1){
				// Update toPlanet
				game.planets.forEach(function(planet){
					if(planet.name == move.toPlanet && move.fleet >= planet.numberOfShips){
						planet.owner = move.player;
						planet.numberOfShips = Math.abs(planet.numberOfShips - move.fleet); 
					}else if(planet.name == move.toPlanet && move.fleet < planet.numberOfShips){
						planet.numberOfShips = planet.numberOfShips - move.fleet; 
					}
				});
				movesToRemove.push(i);
			}else{
				move.duration = move.duration -1;
			}
			
		}
		
		movesToRemove.forEach(function(index){
			game.moves.splice(index);
		});
	}
		

		game.planets.forEach(function(planet){
			planet.numberOfShips += planet.shipRegenRate;
		});
		
		
		if(moves){
			moves.forEach(function(move){
			game.moves.push(move);
		});
		}
		
		
		game.save(function(savedGame){
			callback(game);
		});
	})

}

// Add User adds a user to a current Games players List also assigning a random planet to that user.
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


exports.addPlanetsToGame = function(gameId,planetsToAdd, callback){
	this.findById(gameId, function(game){
		planetsToAdd.forEach(function(planet){
			game.planets.push(planet);
		});
		game.save(function(savedGame){
			callback(savedGame);
		});
	});

}


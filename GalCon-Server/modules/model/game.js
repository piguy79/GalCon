var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
, ObjectId = require('mongoose').Types.ObjectId; 
gamebuilder = require('../gameBuilder');

var gameSchema = mongoose.Schema({
	players : [String],
	width: "Number",
	height: "Number",
	createdDate : "Date",
	currentRound : {
		roundNumber : "Number",
		player : "String"
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

var hasSameOwner = function(planet, move){
	return planet.owner == move.player;
}

var isSamePlanet = function(planet, planetName){
	return planet.name == planetName;
}

var moveHasMoreOrTheSameShipsThenPlanet = function(move, planet){
	return move.fleet >= planet.numberOfShips;
}

var assignPlayerOnJoinGame = function(game, playerWhoJoined){
	if(game.currentRound.player == ""){
		game.currentRound.player = playerWhoJoined;
	}
}

var assignNextCurrentRoundPlayer = function(game, playerWhoJustMoved){
	if(game.hasOnlyOnePlayer()){
		game.currentRound.player = "";
	} else {
		game.currentRound.player = game.nextPlayer(playerWhoJustMoved);
	}
}

gameSchema.methods.nextPlayer = function(playerToSearchFrom){
	var currentIndex = findIndexOfPlayer(this.players, playerToSearchFrom);
	
	if(currentIndex == (this.players.length - 1)){
		return this.players[0];
	}else {
		return this.players[currentIndex + 1]
	}
}

var findIndexOfPlayer = function(players, playerToFindIndexOf){
	for(var i = 0; i < players.length; i++){
		if(players[i] == playerToFindIndexOf){
			return i
		}		
	}
}


gameSchema.methods.applyMoveToPlanets = function(move){

	this.planets.forEach(function(planet){
		if(hasSameOwner(planet, move)){
			planet.numberOfShips = planet.numberOfShips + move.fleet;
		}
		else if(isSamePlanet(move.toPlanet) && moveHasMoreOrTheSameShipsThenPlanet(move, planet)){
			planet.owner = move.player;
			planet.numberOfShips = Math.abs(planet.numberOfShips - move.fleet); 
		}else if(isSamePlanet(move.toPlanet) && !moveHasMoreOrTheSameShipsThenPlanet(move, planet)){
			planet.numberOfShips = planet.numberOfShips - move.fleet; 
		}
	});
}

gameSchema.methods.updateRegenRates = function(){
	this.planets.forEach(function(planet){
		planet.numberOfShips += planet.shipRegenRate;
	});
}

gameSchema.methods.addMoves = function(moves){
	var game = this;
	if(moves){
		moves.forEach(function(move){
			game.moves.push(move);
		});
	}
}

gameSchema.methods.hasOnlyOnePlayer = function(){
	return this.players.length == 1;
}

gameSchema.methods.isLastPlayer = function(player) {
	var currentIndex = findIndexOfPlayer(this.players, player);
	
	if(currentIndex == (this.players.length - 1)){
		return true;
	}
	
	return false;
}



var GameModel = db.model('Game', gameSchema);

exports.createGame = function(players, width, height, numberOfPlanets, callback){
	var game = gamebuilder.createGameBuilder(players, width, height, 10);
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
	if(game.moves && game.isLastPlayer(player)){	
	
		var movesToRemove = [];
		for(var i = 0 ; i < game.moves.length; i++){
			var move = game.moves[i];
			move.duration--;
			if(move.duration == 0){
				game.applyMoveToPlanets(move);
				movesToRemove.push(i);
			}
		}
		
		
		movesToRemove.forEach(function(index){
			game.moves.splice(index);
		});
		
		game.currentRound.roundNumber++;
		
	}
		assignNextCurrentRoundPlayer(game, player);
		game.updateRegenRates();
		game.addMoves(moves);
		
		
		
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
		
		assignPlayerOnJoinGame(game, player);
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


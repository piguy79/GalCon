var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
, ObjectId = require('mongoose').Types.ObjectId; 
gamebuilder = require('../gameBuilder'),
gameTypeAssembler = require('./gameType/gameTypeAssembler'),
rank = require('./rank');



var gameSchema = mongoose.Schema({
	players : [{type: mongoose.Schema.ObjectId, ref: 'User'}],
	width: "Number",
	height: "Number",
	endGameInformation : {
		winnerHandle : "String",
		winningDate : "Date",
		loserHandles : [String],
		draw : "Boolean"
	},
	createdDate : "Date",
	gameType : "String",
	currentRound : {
		roundNumber : "Number",
		playerHandle : "String"
	},
	numberOfPlanets : "Number",
	planets : [
		{
			name : "String",
			ownerHandle : "String",
			isHome : "String",
			position : {
				x : "Number",
				y : "Number"
			},
			shipRegenRate : "Number",
			numberOfShips : "Number",
			ability : "String"
		}
	],
	moves : [
		{
			playerHandle : "String",
			fromPlanet : "String",
			toPlanet : "String",
			fleet : "Number",
			duration : "Number"
		}
	]
	
});

gameSchema.set('toObject', { getters: true });

var hasSameOwner = function(planet, move){
	return planet.ownerHandle == move.playerHandle;
}

var isSamePlanet = function(planet, planetName){
	return planet.name == planetName;
}

var moveHasMoreOrTheSameShipsThenPlanet = function(move, planet){
	return move.fleet >= planet.numberOfShips;
}

var assignPlayerOnJoinGame = function(game, playerWhoJoined){
	if(game.currentRound.playerHandle == ""){
		game.currentRound.playerHandle = playerWhoJoined.handle;
	}
}

var assignNextCurrentRoundPlayer = function(game, playerWhoJustMovedHandle){
	if(game.hasOnlyOnePlayer()){
		game.currentRound.playerHandle = "";
	} else {
		game.currentRound.playerHandle = game.nextPlayer(playerWhoJustMovedHandle);
	}
}

gameSchema.methods.nextPlayer = function(playerHandleToSearchFrom){
	var currentIndex = findIndexOfPlayer(this.players, playerHandleToSearchFrom);
	
	if(currentIndex == (this.players.length - 1)){
		return this.players[0];
	}else {
		return this.players[currentIndex + 1]
	}
}

var findIndexOfPlayer = function(players, playerHandleToFindIndexOf){
	for(var i = 0; i < players.length; i++){
		if(players[i].handle == playerHandleToFindIndexOf){
			return i
		}		
	}
}


gameSchema.methods.applyMoveToPlanets = function(move){

	this.planets.forEach(function(planet){
		if(hasSameOwner(planet, move) && isSamePlanet(planet, move.toPlanet)){
			planet.numberOfShips = planet.numberOfShips + move.fleet;
		}
		else if(isSamePlanet(planet, move.toPlanet) && moveHasMoreOrTheSameShipsThenPlanet(move, planet)){
			planet.ownerHandle = move.playerHandle;
			planet.numberOfShips = Math.abs(planet.numberOfShips - move.fleet); 
		}else if(isSamePlanet(planet, move.toPlanet) && !moveHasMoreOrTheSameShipsThenPlanet(move, planet)){
			planet.numberOfShips = planet.numberOfShips - move.fleet; 
		}
	});
}

gameSchema.methods.updateRegenRates = function(){
	this.planets.forEach(function(planet){
		if(planet.ownerHandle) {
			planet.numberOfShips += planet.shipRegenRate;
		} 
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

gameSchema.methods.isLastPlayer = function(playerHandle) {
	var currentIndex = findIndexOfPlayer(this.players, playerHandle);
	
	if(currentIndex == (this.players.length - 1)){
		return true;
	}
	
	return false;
}



var GameModel = db.model('Game', gameSchema);

exports.createGame = function(players, width, height, numberOfPlanets,gameType, callback){
	var game = gamebuilder.createGameBuilder(players, width, height, numberOfPlanets, gameType);
	game.createBoard(function(createdGame){
		var constructedGame = new GameModel(createdGame);
		constructedGame.populate('players', function(err, game) {
			rank.RankModel.populate(game.players, {path: 'rankInfo'}, function(err, player) {
				callback(game);
			});
		});
	});
};

exports.findAllGames = function(callback){
	GameModel.find({}).populate('players').exec(function(err, games){
		if(err){
			console.log("Unable to find games");
		}else{
			callback(games);
		}
	});
};

exports.findById = function(gameId, callback){
	GameModel.findById(gameId).populate('players').exec(function(err, game){
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


exports.findAvailableGames = function(player, callback){
	GameModel.find().where('players').size(1).populate('players').exec(function(err, games){
		if(err){
			console.log(err);
			next();
		}else{
			// TODO: filter out current player
			callback(games);
		}
	});
};

exports.findCollectionOfGames = function(searchIds, callback){
	GameModel.find({_id : {$in : searchIds}}).populate('players').exec(function(err, games){
		if(err){
			next();		
		}else{
			callback(games);
		}
	});
}


exports.saveGame = function(game, callback) {
	game.save(function(err) {
		if (err) {
			console.log("Something went wrong. " + err);
		} else {
			callback();
		}

	});
}

exports.performMoves = function(gameId, moves, playerHandle, callback) {
	this.findById(gameId, function(game) {
	
		decrementCurrentShipCountOnFromPlanets(game, moves);
		
		if (!game.hasOnlyOnePlayer() && game.isLastPlayer(playerHandle)) {
			processMoves(game, moves);			
			
			gameTypeAssembler.gameTypes[game.gameType].endGameScenario(game);
			gameTypeAssembler.gameTypes[game.gameType].roundProcesser(game);

		} else {
			game.addMoves(moves);
		}

		assignNextCurrentRoundPlayer(game, playerHandle);

		game.save(function(savedGame) {
			callback(game);
		});
	})
}

var decrementCurrentShipCountOnFromPlanets = function(game, moves){

	if(moves){
		for(var i = 0; i < moves.length; i++){
			var fromPlanet = findFromPlanet(game.planets, moves[i].fromPlanet);
			fromPlanet.numberOfShips = fromPlanet.numberOfShips - moves[i].fleet;			
		}
	}
	
}

var findFromPlanet = function(planets, fromPlanetName){
	for(var i = 0; i < planets.length; i++){
		
		if(planets[i].name == fromPlanetName){
			return planets[i];
		}
	}
	
	return "No Planet Found";
}

var processMoves = function(game, newMoves) {
	game.addMoves(newMoves);

	gameTypeAssembler.gameTypes[game.gameType].processMoves(game);
}

// Add User adds a user to a current Games players List also assigning a random
// planet to that user.
exports.addUser = function(gameId, player, callback){
	this.findById(gameId, function(game){
		game.players.push(player);
		
		for(var i in game.planets) {
			var planet = game.planets[i];
			if(!planet.ownerHandle && planet.isHome == "Y") {
				planet.ownerHandle = player.handle;
				break;
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


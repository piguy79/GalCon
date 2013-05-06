var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
, ObjectId = require('mongoose').Types.ObjectId; 
gamebuilder = require('../gameBuilder'),
gameTypeAssembler = require('./gameType/gameTypeAssembler');



var gameSchema = mongoose.Schema({
	players : [String],
	width: "Number",
	height: "Number",
	endGameInformation : {
		winner : "String",
		winningDate : "Date",
		losers : [String],
		draw : "Boolean"
	},
	createdDate : "Date",
	gameType : "String",
	currentRound : {
		roundNumber : "Number",
		player : "String"
	},
	numberOfPlanets : "Number",
	planets : [
		{
			name : "String",
			owner : "String",
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

var moveHasMoreOrTheSameShipsThenPlanet = function(move, planet, enhancedAttackFleet){

	if(enhancedAttackFleet){
		return enhancedAttackFleet >= planet.numberOfShips;
	}
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


gameSchema.methods.applyMoveToPlanets = function(game, move){

	var enhancedAttackFleet;

	if(gameTypeAssembler.gameTypes[game.gameType].findCorrectFleetToAttackEnemyPlanet){
		enhancedAttackFleet = gameTypeAssembler.gameTypes[game.gameType].findCorrectFleetToAttackEnemyPlanet(game.planets, move.player, move.fleet);
	}

	this.planets.forEach(function(planet){
		if(hasSameOwner(planet, move) && isSamePlanet(planet, move.toPlanet)){
			planet.numberOfShips = planet.numberOfShips + move.fleet;
		}
		else if(isSamePlanet(planet, move.toPlanet) && moveHasMoreOrTheSameShipsThenPlanet(move, planet,enhancedAttackFleet)){
			planet.owner = move.player;
			if(enhancedAttackFleet){
				planet.numberOfShips = Math.abs(planet.numberOfShips - enhancedAttackFleet); 
			}else{
				planet.numberOfShips = Math.abs(planet.numberOfShips - move.fleet); 
			}
			planet.numberOfShips = Math.abs(planet.numberOfShips - move.fleet); 
		}else if(isSamePlanet(planet, move.toPlanet) && !moveHasMoreOrTheSameShipsThenPlanet(move, planet,enhancedAttackFleet)){
			planet.numberOfShips = planet.numberOfShips - move.fleet; 
		}
	});
}

gameSchema.methods.updateRegenRates = function(){
	this.planets.forEach(function(planet){
		if(planet.owner) {
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

gameSchema.methods.isLastPlayer = function(player) {
	var currentIndex = findIndexOfPlayer(this.players, player);
	
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


exports.findAvailableGames = function(player, callback){
	GameModel.find({players : {$nin: [player]}}).where('players').size(1).exec(function(err, games){
		if(err){
			console.log(err);
			next();
		}else{
			callback(games);
		}
	});
};

exports.findCollectionOfGames = function(searchIds, callback){
	GameModel.find({_id : {$in : searchIds}}, function(err, games){
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

exports.performMoves = function(gameId, moves, player, callback) {
	this.findById(gameId, function(game) {
	
		decrementCurrentShipCountOnFromPlanets(game, moves);
		
		if (!game.hasOnlyOnePlayer() && game.isLastPlayer(player)) {
			processMoves(game, moves);			
			
			gameTypeAssembler.gameTypes[game.gameType].endGameScenario(game);
			gameTypeAssembler.gameTypes[game.gameType].roundProcesser(game);

		} else {
			game.addMoves(moves);
		}

		assignNextCurrentRoundPlayer(game, player);

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
			if(!planet.owner && planet.isHome == "Y") {
				planet.owner = player;
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


var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
, ObjectId = require('mongoose').Types.ObjectId; 
gamebuilder = require('../gameBuilder'),
gameTypeAssembler = require('./gameType/gameTypeAssembler'),
rank = require('./rank'),
configManager = require('./config'),
positionAdjuster = require('../movement/PositionAdjuster');

var gameSchema = mongoose.Schema({
	version : "Number",
	players : [{type: mongoose.Schema.ObjectId, ref: 'User'}],
	width: "Number",
	height: "Number",
	config : {
		version : "Number",
		values : {}
	},
	endGameInformation : {
		winnerHandle : "String",
		xpAwardToWinner : "Number",
		leaderboardScoreAmount : "Number",
		winningDate : "Date",
		loserHandles : [String],
		draw : "Boolean"
	},
	createdDate : "Date",
	createdTime : "Number",
	map : "Number",
	rankOfInitialPlayer : "Number",
	gameType : "String",
	currentRound : {
		roundNumber : "Number",
		playersWhoMoved : [String]
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
			duration : "Number",
			startingRound : "Number",
			executed : "Boolean",
			previousPosition : {
				x : "Number",
				y : "Number"
			},
			currentPosition : {
				x : "Number",
				y : "Number"
			},
			startPosition : {
				x : "Number",
				y : "Number"
			},
			endPosition : {
				x : "Number",
				y : "Number"
			},
			battlestats : {
				previousShipsOnPlanet : "Number",
				previousPlanetOwner : "String",
				newPlanetOwner : "String",
				defenceStrength : "Number",
				attackStrength : "Number",
				defenceMultiplier : "Number",
				conquer : "Boolean"
			}
		}
	]
	
});

gameSchema.set('toObject', { getters: true });
gameSchema.index({'players' : 1});
gameSchema.index({'endGameInformation.winnerHandle': 1});

var hasSameOwner = function(planet, move){
	return planet.ownerHandle == move.playerHandle;
}

var isSamePlanet = function(planet, planetName){
	return planet.name == planetName;
}

var moveHasMoreOrTheSameShipsThenPlanet = function(fleet, planet){
	return fleet >= planet.numberOfShips;
}

var findIndexOfPlayer = function(players, playerHandleToFindIndexOf){
	for(var i = 0; i < players.length; i++){
		if(players[i].handle == playerHandleToFindIndexOf){
			return i;
		}		
	}
}


gameSchema.methods.applyMoveToPlanets = function(game, move, multiplierMap){
	this.planets.forEach(function(planet){
		if(isADefensiveMoveToThisPlanet(planet, move)){	
			move.battlestats.previousPlanetOwner = planet.ownerHandle;
			move.battlestats.previousShipsOnPlanet = planet.numberOfShips;
			planet.numberOfShips = planet.numberOfShips + move.fleet;
			
		} else if (isSamePlanet(planet, move.toPlanet)){
		
			var defenceMultiplier = 0;
			var attackMultiplier = 0;
			
			if(multiplierMap[planet.ownerHandle]){
				defenceMultiplier = multiplierMap[planet.ownerHandle].defenceMultiplier;
			}
			
			if(multiplierMap[move.playerHandle]){
				attackMultiplier = multiplierMap[move.playerHandle].attackMultiplier;
			}
		
			move.battlestats.previousPlanetOwner = planet.ownerHandle;		
			var defenceStrength = calculateDefenceStrengthForPlanet(planet, game,defenceMultiplier);
			var attackStrength = calculateAttackStrengthForMove(move, game, attackMultiplier);
			var battleResult = defenceStrength - attackStrength;	
			
			move.battlestats.previousShipsOnPlanet = planet.numberOfShips;
			move.battlestats.newPlanetOwner = "";
			move.battlestats.conquer = false;
						
			if(battleResult <= 0){
				move.battlestats.conquer = true;
				move.battlestats.newPlanetOwner = move.playerHandle;
				planet.ownerHandle = move.playerHandle;
				planet.numberOfShips = reverseEffectOfMultiplier(Math.abs(battleResult), attackMultiplier); 
				planet.conquered = true;
			}else{
				move.battlestats.defenceMultiplier = defenceMultiplier;
				planet.numberOfShips = reverseEffectOfMultiplier(battleResult,defenceMultiplier);
			}
			
			
			move.battlestats.defenceStrength = defenceStrength;
			move.battlestats.attackStrength = attackStrength;
		}
		
	});
}

var reverseEffectOfMultiplier = function(battleResult, multiplierValue){
	return battleResult * (1 / (1 + multiplierValue));
}

gameSchema.methods.allPlayersHaveTakenAMove = function(){
	return this.currentRound.playersWhoMoved.length == this.players.length;
}

var getDefenceMutlipler = function(player, game){
	var enhancedDefence = 0;

	if(gameTypeAssembler.gameTypes[game.gameType].findCorrectDefenseForAPlanet){
		enhancedDefence = gameTypeAssembler.gameTypes[game.gameType].findCorrectDefenseForAPlanet(game.config, game.planets, player);	
	}
	
	return enhancedDefence;
}

var getAttackMultipler = function(player, game){
	var enhancedAttackFleet = 0;

	if(gameTypeAssembler.gameTypes[game.gameType].findCorrectFleetToAttackEnemyPlanet){
		enhancedAttackFleet = gameTypeAssembler.gameTypes[game.gameType].findCorrectFleetToAttackEnemyPlanet(game.config, game.planets, player);	
	}
	
	return enhancedAttackFleet;
}

var calculateAttackStrengthForMove = function(move, game, attackMultiplier){
	return move.fleet + (move.fleet * attackMultiplier);
}

var calculateDefenceStrengthForPlanet = function(planet, game, defenceMultiplier){
	return planet.numberOfShips + (planet.numberOfShips * defenceMultiplier);
}

var isADefensiveMoveToThisPlanet = function(planet, move){
	return hasSameOwner(planet, move) && isSamePlanet(planet, move.toPlanet);
}


gameSchema.methods.updateRegenRates = function(){

	var currentGame = this;
	this.planets.forEach(function(planet){
		if(planet.ownerHandle && !planet.conquered) {
		
			var regenBy = planet.shipRegenRate;
		
			if(gameTypeAssembler.gameTypes[currentGame.gameType].determineIfAnOpponentHasTheRegenBlock){
				blockRegen = gameTypeAssembler.gameTypes[currentGame.gameType].determineIfAnOpponentHasTheRegenBlock(currentGame, planet.ownerHandle);	
				
				if(blockRegen){
					regenBy =  planet.shipRegenRate - (planet.shipRegenRate * currentGame.config.values['blockAbility']);
				}
			}
			
			if(regenBy < 1){
				regenBy = 1;
			}
			
			planet.numberOfShips += regenBy;
			
		} 
	});
}

gameSchema.methods.addMoves = function(moves){
	var game = this;
	if(moves){
		moves.forEach(function(move){
			move.startingRound = game.currentRound.roundNumber;
			game.moves.push(move);
		});
	}
}

gameSchema.methods.hasOnlyOnePlayer = function(){
	return this.players.length == 1;
}

var GameModel = db.model('Game', gameSchema);

exports.createGame = function(gameAttributes) {
	var game = gamebuilder.createGameBuilder(gameAttributes);
	game.createBoard();

	var p = configManager.findLatestConfig("map");
	return p.then(function(config) {
		var constructedGame = new GameModel(game);
		constructedGame.config = config;
		
		var innerp = new mongoose.Promise();
		constructedGame.populate('players', function(err, gameWithPlayers) {
			if(err) { innerp.reject(err); }
			else { innerp.complete(gameWithPlayers); }
		});
		return innerp;
	});
};

exports.findAllGames = function() {
	return GameModel.find({}).populate('players').exec();
}

exports.findById = function(gameId){
	return GameModel.findById(gameId).populate('players').exec();
};

exports.deleteGame = function(gameId){
	GameModel.findById(gameId).remove();
};


exports.findAvailableGames = function(player) {
	var p = GameModel.find().where('players').size(1).populate('players').exec();
	return p.then(function(games) {
		var filteredGames = [];
		games.forEach(function(game) {
			if(game.players[0].handle != player) {
				filteredGames.push(game);
			}
		});
		
		return filteredGames;	
	});
};

exports.findCollectionOfGames = function(searchIds){
	return GameModel.find({_id : {$in : searchIds}}).populate('players').exec();
}

exports.saveGame = function(game) {
	var p = new mongoose.Promise();
	game.save(function(err, savedGame) {
		if (err) {
			p.reject(err);
		} else {
			p.complete(savedGame);
		}
	});
	return p;
}

exports.performMoves = function(gameId, moves, playerHandle, attemptNumber) {
	if(attemptNumber > 5) {
		var p = new mongoose.Promise();
		p.reject("Too many attempts to perform move");
		return p;
	}
	
	var p = exports.findById(gameId);
	p.then(function(game) {
		game.addMoves(moves);
		game.currentRound.playersWhoMoved.push(playerHandle);
			
		if (!game.hasOnlyOnePlayer() && game.allPlayersHaveTakenAMove()) {
		
		    removeMovesWhichHaveBeenExecuted(game);
			decrementCurrentShipCountOnFromPlanets(game);
			game.currentRound.playersWhoMoved = [];
			
			processMoves(playerHandle, game);
			
			gameTypeAssembler.gameTypes[game.gameType].endGameScenario(game);
			gameTypeAssembler.gameTypes[game.gameType].roundProcesser(game);
		} 

		var existingVersion = game.version;
		game.version++;
		
		// Need to update the entire document.  Remove the _id b/c Mongo won't accept _id as a field to update.
		var updatedGame = game;
		delete updatedGame._doc._id
		var updatePromise = GameModel.findOneAndUpdate({_id: gameId, version: existingVersion}, updatedGame._doc).exec();
		return updatePromise.then(function(savedGame) {
			if(!savedGame) {
				return exports.performMoves(gameId, moves, playerHandle, attemptNumber + 1);
			} else {
				savedGame.populate('players', function(err, game) {
					return game;
				});
			}
		});
	})
}

var removeMovesWhichHaveBeenExecuted = function(game){
	var i = game.moves.length;
	while (i--) {
		var move = game.moves[i];
		if (move.executed) {
			game.moves.splice(i, 1)
		}
	}
}

var decrementCurrentShipCountOnFromPlanets = function(game){

	if(game.moves){
		for(var i = 0; i < game.moves.length; i++){
			var move = game.moves[i];
			
			if(move.startingRound == game.currentRound.roundNumber){
				var fromPlanet = findFromPlanet(game.planets, game.moves[i].fromPlanet);
				fromPlanet.numberOfShips = fromPlanet.numberOfShips - game.moves[i].fleet;
			}			
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

var opponent = function(player, game){
	for(var i = 0;i < game.players.length; i++){
		if(game.players[i].handle != player){
			return game.players[i].handle;
		}
	}
	
	return "";
}

var processMoves = function(player, game) {

	var multiplierMap = {
	};
	var currentOpponent = opponent(player, game);
	
	multiplierMap[''] = {
			attackMultiplier : 0,
			defenceMultiplier : 0
		};
	
	multiplierMap[player] = {
			attackMultiplier : getAttackMultipler(player, game),
			defenceMultiplier : getDefenceMutlipler(player,game)
		};
	
	multiplierMap[currentOpponent] = {
			attackMultiplier : getAttackMultipler(currentOpponent, game),
			defenceMultiplier : getDefenceMutlipler(currentOpponent,game)
		};

	gameTypeAssembler.gameTypes[game.gameType].processMoves(game, multiplierMap);
	
	positionAdjuster.adjustMovePositions(game);
}


// Add User adds a user to a current Games players List also assigning a random
// planet to that user.
exports.addUser = function(gameId, player){
	var p = GameModel.findOneAndUpdate({ $and : [{_id : gameId}, { $where : "this.players.length == 1"}]}, {$push : {players : player}}).exec();
	return p.then(function(game) {
		if(!game) {
			return null;
		} else {
			for(var i in game.planets) {
				var planet = game.planets[i];
				if(!planet.ownerHandle && planet.isHome == "Y") {
					planet.ownerHandle = player.handle;
					break;
				}
			}
		
			return exports.saveGame(game);
		}
	});
}

exports.addPlanetsToGame = function(gameId,planetsToAdd){
	var p = this.findById(gameId);
	return p.then(function(game) {
		planetsToAdd.forEach(function(planet){
			game.planets.push(planet);
		});
		
		return exports.saveGame(game);
	});
}

exports.findGameForMapInTimeLimit = function(mapToFind, time, playerHandle){
	var p = GameModel.find({ $and  : [{ $where : "this.players.length == 1"}, {map : mapToFind}, {createdTime : { $lt : time}}]}).populate('players').exec();
	return p.then(function(games) {
		return filterOutPlayer(games, playerHandle);
	});
}

exports.findGameAtAMap = function(mapToFind, playerHandle){
	var p = GameModel.find({ $and : [{ $where : "this.players.length == 1"}, {map : mapToFind}]}).populate('players').sort({rankOfInitialPlayer : 1}).exec();
	return p.then(function(games) {
		return filterOutPlayer(games, playerHandle);
	});
}

var filterOutPlayer = function(games, playerHandle){
	var filteredGames = [];
	games.forEach(function(game) {
		if(game.players[0].handle != playerHandle) {
			filteredGames.push(game);
		}
	});
	
	return filteredGames;
}

exports.GameModel = GameModel;

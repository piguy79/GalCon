var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
, ObjectId = require('mongoose').Types.ObjectId; 
gamebuilder = require('../gameBuilder'),
gameTypeAssembler = require('./gameType/gameTypeAssembler'),
rank = require('./rank'),
configManager = require('./config'),
positionAdjuster = require('../movement/PositionAdjuster'),
_ = require('underscore'),
abilityBasedGameType = require('./gameType/abilityBasedGameType');


var gameSchema = mongoose.Schema({
	version : "Number",
	players : [{type: mongoose.Schema.ObjectId, ref: 'User'}],
	width: "Number",
	height: "Number",
	social: {
		invitee : "String",
		status : "String"
	},
	config : {
		version : "Number",
		values : {}
	},
	endGame : {
		winnerHandle : "String",
		xpAwardToWinner : "Number",
		leaderboardScoreAmount : "Number",
		winningDate : "Date",
		loserHandles : [String],
		draw : "Boolean",
		declined : "String"
	},
	createdDate : "Date",
	createdTime : "Number",
	map : "Number",
	rankOfInitialPlayer : "Number",
	gameType : "String",
	round : {
		num : "Number",
		moved : [String]
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
			regen : "Number",
			ships : "Number",
			population : "Number",
			ability : "String",
			harvest : {
				status : "String",
				startingRound : "Number",
				saveRound : "Number"
			},
			status : "String"
		}
	],
	moves : [
		{
			handle : "String",
			from : "String",
			to : "String",
			fleet : "Number",
			duration : "Number",
			startingRound : "Number",
			executed : "Boolean",
			prevPos : {
				x : "Number",
				y : "Number"
			},
			curPos : {
				x : "Number",
				y : "Number"
			},
			startPos : {
				x : "Number",
				y : "Number"
			},
			endPos : {
				x : "Number",
				y : "Number"
			},
			bs : {
				previousShipsOnPlanet : "Number",
				previousPlanetOwner : "String",
				atckMult : "Number",
				defMult : "Number",
				diaa : "Boolean",
				startFleet : "Number"
			}
		}
	]
});

gameSchema.set('toObject', { getters: true });
gameSchema.index({'players' : 1});
gameSchema.index({'endGame.winnerHandle': 1});
gameSchema.index({'social.invitee': 1});



var hasSameOwner = function(planet, move){
	return planet.ownerHandle == move.handle;
}

var isSamePlanet = function(planet, planetName){
	return planet.name == planetName;
}

var moveHasMoreOrTheSameShipsThenPlanet = function(fleet, planet){
	return fleet >= planet.ships;
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
		if(isADefensiveMoveToThisPlanet(planet, move)) {	
			move.bs.previousPlanetOwner = planet.ownerHandle;
			move.bs.previousShipsOnPlanet = planet.ships;
			planet.ships = planet.ships + move.fleet;
		} else if (isSamePlanet(planet, move.to)) {
			var defenceMultiplier = 0;
			var attackMultiplier = 0;
			
			if (multiplierMap[planet.ownerHandle]) {
				defenceMultiplier = multiplierMap[planet.ownerHandle].defenceMultiplier;
			}
			
			if (multiplierMap[move.handle]) {
				attackMultiplier = multiplierMap[move.handle].attackMultiplier;
			}
		
			move.bs.previousPlanetOwner = planet.ownerHandle;		
			var defenceStrength = calculateDefenceStrengthForPlanet(planet, defenceMultiplier);
			var attackStrength = game.calculateAttackStrengthForMove(move, attackMultiplier);
			var battleResult = defenceStrength - attackStrength;	
			
			move.bs.previousShipsOnPlanet = planet.ships;
						
			if(battleResult <= 0) {
				planet.ownerHandle = move.handle;
				planet.ships = game.reverseEffectOfMultiplier(Math.abs(battleResult), attackMultiplier); 
				planet.conquered = true;
				checkHarvestStatus(planet,game.round.num, parseFloat(game.config.values["harvestSavior"]));
			} else {
				planet.ships = game.reverseEffectOfMultiplier(battleResult,defenceMultiplier);
			}
			
			move.bs.defMult = defenceMultiplier;
			move.bs.atckMult = attackMultiplier;
		}
	});
}

var checkHarvestStatus = function(planet, roundNumber, saviorBonus){
	if(planet.harvest && planet.harvest.status === "ACTIVE"){
		planet.harvest.status = "INACTIVE";
		planet.harvest.saveRound = roundNumber;
		planet.ships += saviorBonus;
	}else if(planet.harvest && planet.harvest.status === "INACTIVE"){
		planet.harvest = null;
	}
}

gameSchema.methods.reverseEffectOfMultiplier = function(battleResult, multiplierValue){
	return parseInt(battleResult * (1 / (1 + multiplierValue)), 10);
}

gameSchema.methods.allPlayersHaveTakenAMove = function(){
	return this.round.moved.length == this.players.length;
}

var getDefenceMutlipler = function(player, game){
	var enhancedDefence = 0;

	if(gameTypeAssembler.gameTypes[game.gameType].findCorrectDefenseForAPlanet){
		enhancedDefence = gameTypeAssembler.gameTypes[game.gameType].findCorrectDefenseForAPlanet(game.config, game.planets, player);	
		enhancedDefence +=  abilityBasedGameType.harvestEnhancement(player, game);
	}
	
	return enhancedDefence;
}

var getAttackMultipler = function(player, game){
	var enhancedAttackFleet = 0;

	if(gameTypeAssembler.gameTypes[game.gameType].findCorrectFleetToAttackEnemyPlanet){
		enhancedAttackFleet = gameTypeAssembler.gameTypes[game.gameType].findCorrectFleetToAttackEnemyPlanet(game.config, game.planets, player);

		enhancedAttackFleet += abilityBasedGameType.harvestEnhancement(player, game);

	}
	
	return enhancedAttackFleet;
}



gameSchema.methods.calculateAttackStrengthForMove = function(move, attackMultiplier) {
	return move.fleet + (move.fleet * attackMultiplier);
}

var calculateDefenceStrengthForPlanet = function(planet, defenceMultiplier){
	return planet.ships + (planet.ships * defenceMultiplier);
}

var isADefensiveMoveToThisPlanet = function(planet, move){
	return hasSameOwner(planet, move) && isSamePlanet(planet, move.to);
}


gameSchema.methods.updateRegenRates = function(){

	var currentGame = this;
	this.planets.forEach(function(planet){
		if(planet.ownerHandle && !planet.conquered && planet.status === 'ALIVE') {
		
			var regenBy = planet.regen;
		
			if(gameTypeAssembler.gameTypes[currentGame.gameType].determineIfAnOpponentHasTheRegenBlock){
				blockRegen = gameTypeAssembler.gameTypes[currentGame.gameType].determineIfAnOpponentHasTheRegenBlock(currentGame, planet.ownerHandle);	
				
				if(blockRegen){
					var blockModifier = currentGame.config.values['blockAbility'] + abilityBasedGameType.harvestEnhancement(opponent(planet.ownerHandle), currentGame);
					regenBy =  planet.regen - (planet.regen * blockModifier);
				}
			}
			
			if(regenBy < 1){
				regenBy = 1;
			}
			
			planet.ships += regenBy;
			
		} 
	});
}

gameSchema.methods.addMoves = function(moves){
	var game = this;
	if(moves){
		moves.forEach(function(move){
			move.startingRound = game.round.num;
			game.moves.push(move);
		});
	}
}

gameSchema.methods.addHarvest = function(harvest){
	var game = this;
	if(harvest){
		_.each(harvest, function(item){
			var planet = _.find(game.planets, function(planet){return planet.name === item.planet});
			planet.harvest = {status : "ACTIVE", startingRound : game.round.num};
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

exports.findById = function(gameId){
	return GameModel.findById(gameId).populate('players').exec();
};


exports.findCollectionOfGames = function(user){
	return GameModel.find({players : {$in : [user._id]}}, '_id players endGame createdDate round map social').populate('players').exec();
}

exports.performMoves = function(gameId, moves, playerHandle, attemptNumber, harvest) {
	if(attemptNumber > 5) {
		var p = new mongoose.Promise();
		p.reject("Too many attempts to perform move");
		return p;
	}
	
	var roundExecuted = false;
	var finalGameForReturn;
	
	var p = exports.findById(gameId);
	return p.then(function(game) {
		game.addMoves(moves);
		game.addHarvest(harvest);
		game.round.moved.push(playerHandle);
			
		if (!game.hasOnlyOnePlayer() && game.allPlayersHaveTakenAMove()) {
		
		    removeMovesWhichHaveBeenExecuted(game);
			decrementCurrentShipCountOnFromPlanets(game);
			destroyAbilityPlanets(game);
			decreasePopulationIfUnderHarvest(game);
			game.round.moved = [];
			
			processMoves(playerHandle, game);
			
			gameTypeAssembler.gameTypes[game.gameType].endGameScenario(game);
			gameTypeAssembler.gameTypes[game.gameType].roundProcesser(game);
			roundExecuted = true;
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
				return savedGame.withPromise(savedGame.populate, 'players');
			}
		}).then(function(finalGame){
			finalGameForReturn = finalGame;
			return updatePlayerXpForPlanetCapture(finalGame, roundExecuted);
		}).then(function(){
			var returnP = new mongoose.Promise();
			returnP.complete(finalGameForReturn);
			return returnP;
		});
	});
}

var updatePlayerXpForPlanetCapture = function(game, roundExecuted){
	var promise = new mongoose.Promise();
	promise.complete();
	var lastPromise = promise;	
	
	
	game.players.forEach(function(player){
		lastPromise = lastPromise.then(function(){
			if(roundExecuted){
				var conqueredPlanets = _.filter(game.moves, function(move){
					return move.executed && move.handle === player.handle && move.bs.previousPlanetOwner !== player.handle; 
				});
				
				var xpForPlayer = game.config.values['xpForPlanetCapture'] * conqueredPlanets.length;
				player.xp += xpForPlayer;
			}
			
			return player.withPromise(player.save);
		});
	});
	
	
	return lastPromise;
}

var planetShouldBeDestroyed = function(game, planet){
	var roundsPassedWithHarvest = game.round.num - planet.harvest.startingRound;
	return roundsPassedWithHarvest >= game.config.values['harvestRounds'];
}

var destroyAbilityPlanets = function(game){
	_.each(game.planets, function(planet){
		if(planet.harvest && planet.harvest.status === 'ACTIVE' && planetShouldBeDestroyed(game, planet)){
			planet.regen = 0;
			planet.status = "DEAD";
		}
	})
}

var decreasePopulationIfUnderHarvest = function(game){
	_.each(game.planets, function(planet){
		if(planet.population <= 0){
			planet.population = 0;
		}else if(planet.harvest && planet.harvest.status === 'ACTIVE'){
			var roundsLeft = game.config.values['harvestRounds'] - (game.round.num - planet.harvest.startingRound);
			if(roundsLeft <= 0){
				planet.population = 0;
			}else{
				planet.population = planet.population - (planet.population / roundsLeft);
			}
		}
	});
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
			
			if(move.startingRound == game.round.num){
				var fromPlanet = findFromPlanet(game.planets, game.moves[i].from);
				fromPlanet.ships = fromPlanet.ships - game.moves[i].fleet;
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
	var multiplierMap = {};
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
	var p = GameModel.findOneAndUpdate({ $and : [{_id : gameId}, { $where : "this.players.length == 1"}]}, {$push : {players : player}}).populate('players').exec();
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
		
			return game.withPromise(game.save);
		}
	});
}

exports.addSocialUser = function(gameId, player){
	var p = GameModel.findOneAndUpdate({ $and : [{_id : gameId}, {'social.invitee' : player.handle}, {'social.status' : 'CREATED'}]}, {$push : {players : player}, $set : {'social.status' : 'ACCEPTED'}}).populate('players').exec();
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
			
			return game.withPromise(game.save);
		}
	});
}

exports.declineSocialGame = function(gameId, invitee){
	var endResult = {
			xpAwardToWinner : 0,
			leaderboardScoreAmount : 0,
			winningDate : Date.now(),
			draw : false,
			declined : invitee
		};

	return GameModel.findOneAndUpdate({_id : gameId}, 
									{
										$set : {
												   'social.status' : 'DECLINED'
												   ,'endGame.xpAwardToWinner' : 0
												   ,'endGame.leaderboardScoreAmount' : 0
												   ,'endGame.winningDate' : 0
												   ,'endGame.draw' : 0
												   ,'endGame.declined' : invitee
											   }}).populate('players').exec();
}

exports.findGameForMapInTimeLimit = function(mapToFind, time, playerHandle){
	var p = GameModel.find({ $and  : [{ $where : "this.players.length == 1"}, {map : mapToFind}, {createdTime : { $lt : time}}]}).populate('players').exec();
	return p.then(function(games) {
		return filterOutPlayerAndSocial(games, playerHandle);
	});
}

exports.findGameAtAMap = function(mapToFind, playerHandle){
	var p = GameModel.find({ $and : [{ $where : "this.players.length == 1"}, {map : mapToFind}]}).populate('players').sort({rankOfInitialPlayer : 1}).exec();
	return p.then(function(games) {
		return filterOutPlayerAndSocial(games, playerHandle);
	});
}

var filterOutPlayerAndSocial = function(games, playerHandle){
	var filteredGames = _.filter(games, function(game){
		return game.players[0].handle != playerHandle;
	});
	
	return filteredGames;
}

exports.findByInvitee = function(inviteeHandle){
	return GameModel.find({'social.invitee' : inviteeHandle, 'social.status' : 'CREATED'}).populate('players').exec();
}

exports.GameModel = GameModel;

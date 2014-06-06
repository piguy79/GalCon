var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
, ObjectId = require('mongoose').Types.ObjectId; 
gamebuilder = require('../gameBuilder'),
gameAi = require('../gameAi'),
gameTypeAssembler = require('./gameType/gameTypeAssembler'),
rank = require('./rank'),
userManager = require('./user'),
configManager = require('./config'),
mapManager = require('./map'),
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
		xp : "Number",
		leaderboardScoreAmount : "Number",
		date : "Date",
		loserHandles : [String],
		declined : "String",
		viewedBy : [String]
	},
	state : "String",
	createdDate : "Date",
	moveTime : 'Number',
	map : "Number",
	rankOfInitialPlayer : "Number",
	gameType : "String",
	round : {
		num : "Number",
		moved : [String]
	},
	ai : "Boolean",
	planets : [
		{
			name : "String",
			handle : "String",
			isHome : "String",
			pos : {
				x : "Number",
				y : "Number"
			},
			regen : "Number",
			ships : "Number",
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
			bs : {
				prevShipsOnPlanet : "Number",
				prevPlanetOwner : "String",
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
	return planet.handle == move.handle;
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
			move.bs.prevPlanetOwner = planet.handle;
			move.bs.prevShipsOnPlanet = planet.ships;
			planet.ships = planet.ships + move.fleet;
		} else if (isSamePlanet(planet, move.to)) {
			var defenceMultiplier = 0;
			var attackMultiplier = 0;
			
			if (multiplierMap[planet.handle]) {
				defenceMultiplier = multiplierMap[planet.handle].defenceMultiplier;
			}
			
			if (multiplierMap[move.handle]) {
				attackMultiplier = multiplierMap[move.handle].attackMultiplier;
			}
		
			move.bs.prevPlanetOwner = planet.handle;		
			var defenceStrength = calculateDefenceStrengthForPlanet(planet, defenceMultiplier);
			var attackStrength = game.calculateAttackStrengthForMove(move, attackMultiplier);
			var battleResult = defenceStrength - attackStrength;	
			
			move.bs.prevShipsOnPlanet = planet.ships;
						
			if(battleResult <= 0) {
				planet.handle = move.handle;
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
		planet.harvest = undefined;
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
		enhancedDefence = gameTypeAssembler.gameTypes[game.gameType].findCorrectDefenseForAPlanet(game.config, game.planets, player, game);	
	}
	
	return enhancedDefence;
}

var getAttackMultipler = function(player, game){
	var enhancedAttackFleet = 0;

	if(gameTypeAssembler.gameTypes[game.gameType].findCorrectFleetToAttackEnemyPlanet){
		enhancedAttackFleet = gameTypeAssembler.gameTypes[game.gameType].findCorrectFleetToAttackEnemyPlanet(game.config, game.planets, player, game);
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
		if(planet.handle && !planet.conquered) {
		
			var regenBy = planet.regen;
		
			if(gameTypeAssembler.gameTypes[currentGame.gameType].determineIfAnOpponentHasTheRegenBlock){
				blockRegen = gameTypeAssembler.gameTypes[currentGame.gameType].determineIfAnOpponentHasTheRegenBlock(currentGame, planet.handle);	
				
				if(blockRegen){
					var blockModifier = currentGame.config.values['blockAbility'] + abilityBasedGameType.harvestEnhancement(opponent(planet.handle), currentGame);
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
			if(move.curPos){
				move.curPos = {x : move.curPos.x, y : move.curPos.y};
			}
			if(move.prevPos){
				move.prevPos = {x : move.prevPos.x, y : move.prevPos.y};
			}
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
			else { innerp.fulfill(gameWithPlayers); }
		});
		return innerp;
	});
};

exports.findById = function(gameId){
	return GameModel.findById(gameId).populate('players').exec();
};

exports.findByIdAndAddViewed = function(gameId, handle) {
	return GameModel.findOneAndUpdate({_id: gameId}, {$push : {'endGame.viewedBy' : handle}}).populate('players').exec();
};

exports.findCollectionOfGames = function(user, filters){
	var query = {players : {$in : [user._id]}};
	if(filters) {
		query = _.extend(query, filters);
	}
	console.log(query);
	return GameModel.find(query, '_id players endGame createdDate round map social config moveTime').populate('players').exec();
}

exports.performMoves = function(gameId, moves, playerHandle, attemptNumber, harvest) {
	if(attemptNumber > 5) {
		var p = new mongoose.Promise();
		p.reject("Too many attempts to perform move");
		return p;
	}
	
	var roundExecuted = false;
	var game;
	var map;
	
	var p = exports.findById(gameId);
	return p.then(function(associatedGame){
		game = associatedGame;
	}).then(function(){
		return mapManager.findMapByKey(game.map);
	}).then(function(associatedMap) {
		map = associatedMap;
		game.addMoves(moves);
		game.addHarvest(harvest);
		game.round.moved.push(playerHandle);
		game.moveTime = Date.now();
		
		if(game.ai){
			game.addMoves(gameAi.createAiMoves(game, map));
			game.addHarvest(gameAi.createHarvest(game, map));
			game.round.moved.push('AI');
		}
			
		if (!game.hasOnlyOnePlayer() && game.allPlayersHaveTakenAMove()) {
		    removeMovesWhichHaveBeenExecuted(game);
			decrementCurrentShipCountOnFromPlanets(game);
			destroyAbilityPlanets(game);
			
			processMoves(playerHandle, game);
			
			gameTypeAssembler.gameTypes[game.gameType].endGameScenario(game);
			gameTypeAssembler.gameTypes[game.gameType].roundProcesser(game);
			roundExecuted = true;
		} 

		var existingVersion = game.version;
		game.version++;
		
		// Need to update the entire document.  Remove the _id b/c Mongo won't accept _id as a field to update.
		var updatedGame = game;
		if (!game.hasOnlyOnePlayer() && game.allPlayersHaveTakenAMove()) {
			updatedGame.round.moved = [];
		}
		
		delete updatedGame._doc._id
		var updatePromise = GameModel.findOneAndUpdate({_id: gameId, version: existingVersion, 'round.moved' : {$nin : [playerHandle]}}, updatedGame._doc).exec();
		return updatePromise.then(function(savedGame) {
			if(!savedGame) {
				return exports.performMoves(gameId, moves, playerHandle, attemptNumber + 1);
			} else {
				return savedGame.withPromise(savedGame.populate, 'players');
			}
		}).then(function(finalGame){
			return updatePlayerXpForPlanetCapture(finalGame, roundExecuted);
		}).then(function(){
			return exports.findById(gameId);
		});
	});
}

var updatePlayerXpForPlanetCapture = function(game, roundExecuted){
	var promise = new mongoose.Promise();
	promise.fulfill();
	var lastPromise = promise;	
	
	game.players.forEach(function(player){
		lastPromise = lastPromise.then(function(){
			var xpToUpdate = 0;
			if(roundExecuted){
				var conqueredPlanets = _.chain(game.moves).filter(function(move){
					return move.executed && move.handle === player.handle && move.bs.prevPlanetOwner !== player.handle; 
				}).map(function(move) { return move.to; }).uniq().value();
				
				var xpForPlayer = game.config.values['xpForPlanetCapture'] * conqueredPlanets.length;
				xpToUpdate = xpForPlayer;
			}
			
			return exports.updatePlayerXp(player.handle, game, xpToUpdate, 0);
		});
	});
	
	return lastPromise;
}

exports.updatePlayerXp = function(handle, game, xpToAdd, attemptNumber){
	if(attemptNumber > 5) {
		var p = new mongoose.Promise();
		p.reject("Too many attempts to update player xp");
		return p;
	}
	
	var currentUser;
	var p = userManager.findUserByHandle(handle);
	return p.then(function(user){
		currentUser = user;
		return rank.findAllRanks();
	}).then(function(ranks){
		var maxRank= _.last(ranks);
		var potentialNewXp = currentUser.xp + parseInt(game.config.values["xpForWinning"]);
		if(potentialNewXp >= maxRank.endAt){
			xpToAdd = (maxRank.endAt - 1) - currentUser.xp;
		}
		return userManager.UserModel.findOneAndUpdate({handle : handle, xp : currentUser.xp}, {$inc : {xp : xpToAdd}}).exec();
	}).then(function(user){
		if(!user){
			return exports.updatePlayerXp(handle, existingXp, xpToAdd, attemptNumber + 1);
		}else{
			return userManager.findUserByHandle(handle);
		}
	});
}

var planetShouldBeDestroyed = function(game, planet){
	var roundsPassedWithHarvest = game.round.num - planet.harvest.startingRound;
	return roundsPassedWithHarvest >= game.config.values['harvestRounds'];
}

var destroyAbilityPlanets = function(game){
	_.each(game.planets, function(planet){
		if(planet.harvest && planet.harvest.status === 'ACTIVE' && planetShouldBeDestroyed(game, planet)){
			planet.status = "DEAD";
			planet.harvest.status = "INACTIVE";
		}
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
	var p = GameModel.findOneAndUpdate({ $and : [{_id : gameId}, {$or : [{social : {$exists : false}}, {'social.status' : null}]}, { $where : "this.players.length == 1"}, {state : {$ne : 'C'}}]}, {$push : {players : player}}).populate('players').exec();
	return p.then(function(game) {
		if(!game) {
			return null;
		} else {
			for(var i in game.planets) {
				var planet = game.planets[i];
				if(!planet.handle && planet.isHome == "Y") {
					planet.handle = player.handle;
					break;
				}
			}
		
			return game.withPromise(game.save);
		}
	});
}

exports.addSocialUser = function(gameId, player){
	var p = GameModel.findOneAndUpdate({ $and : [{_id : gameId}, { $where : "this.players.length == 1"} , {'social.invitee' : player.handle}, {'social.status' : 'CREATED'}, {state : {$ne : 'C'}}]}, {$push : {players : player}, $set : {'social.status' : 'ACCEPTED'}}).populate('players').exec();
	return p.then(function(game) {
		if(!game) {
			return null;
		} else {
			for(var i in game.planets) {
				var planet = game.planets[i];
				if(!planet.handle && planet.isHome == "Y") {
					planet.handle = player.handle;
					break;
				}
			}
			
			return game.withPromise(game.save);
		}
	});
}

exports.declineSocialGame = function(gameId, invitee){
	var endResult = {
			xp : 0,
			leaderboardScoreAmount : 0,
			date : Date.now(),
			declined : invitee
		};

	return GameModel.findOneAndUpdate({_id : gameId}, 
									{
										$set : {
												   'social.status' : 'DECLINED'
												   ,'endGame.xp' : 0
												   ,'endGame.leaderboardScoreAmount' : 0
												   ,'endGame.date' : 0
												   , 'endGame.winnerHandle' : 'GAME_DECLINE'
												   ,'endGame.declined' : invitee
											   }}).populate('players').exec();
}

exports.findGameForMapInTimeLimit = function(mapToFind, time, playerHandle){
	var p = GameModel.find({ $and  : [{ $where : "this.players.length == 1"}, {$or : [{social : {$exists : false}}, {'social.status' : null}]}, {map : mapToFind}, {state : {$ne : 'C'}}, {createdDate : { $lt : time}}]}).populate('players').exec();
	return p.then(function(games) {
		return filterOutPlayerAndSocial(games, playerHandle);
	});
}

exports.findGameAtAMap = function(mapToFind, playerHandle){
	var p = GameModel.find({ $and : [{ $where : "this.players.length == 1"},{$or : [{social : {$exists : false}}, {'social.status' : null}]}, {map : mapToFind}, {state : {$ne : 'C'}}]}).populate('players').sort({rankOfInitialPlayer : 1}).exec();
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

exports.claimVictory = function(gameId, moveTime, currentUser, loserHandle){
	return GameModel.findOneAndUpdate({_id : gameId, moveTime : moveTime}, {
		$set : {
			   'endGame.xp' : 50,
			   'endGame.date' : Date.now(),
			   'endGame.winnerHandle' : currentUser.handle,
			   'endGame.loserHandles' : [loserHandle]
		   }}).populate('players').exec();
}

exports.GameModel = GameModel;

var mongoose = require('../modules/model/mongooseConnection').mongoose,
	gameBuilder = require('../modules/gameBuilder'), 
	gameManager = require('../modules/model/game'), 
	userManager = require('../modules/model/user'), 
	rankManager = require('../modules/model/rank'), 
	mapManager = require('../modules/model/map'), 
	configManager = require('../modules/model/config'), 
	leaderboardManager = require('../modules/model/leaderboard'), 
	inventoryManager = require('../modules/model/inventory'), 
	_ = require('underscore');

exports.index = function(req, res) {
	res.render('index.html')
};

exports.findAllGames = function(req, res) {
	gameManager.findAllGames(function(games) {
		var returnObj = {};
		returnObj.items = games;
		res.json(returnObj);
	});
}

exports.findGameById = function(req, res) {
	var searchId = req.query['id'];
	var playerHandle = req.query['playerHandle'];
	
	var p = gameManager.findById(searchId);
	p.then(function(game) {
		res.json(processGameReturn(game, playerHandle));
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.findAvailableGames = function(req, res) {
	var playerHandle = req.query['playerHandle'];
	gameManager.findAvailableGames(playerHandle, function(games) {
		var returnObj = {};
		returnObj.items = games;
		res.json(returnObj);
	});
}

exports.findGamesWithPendingMove = function(req, res) {
	var userName = req.query['userName'];
	userManager.findUserByName(userName, function(user) {
		if (!user) {
			res.json({});
		} else {
			gameManager.findCollectionOfGames(user.currentGames, function(games) {
				var returnObj = {};
				var len = games.length;
				while (len--) {
					if (games[len].currentRound.playersWhoMoved.indexOf(user.handle) >= 0
							|| games[len].endGameInformation.winnerHandle) {
						games.splice(len, 1);
					}
				}
				returnObj.items = games;
				res.json(returnObj);
			});
		}
	});
}

exports.findUserByUserName = function(req, res) {
	var userName = req.query['userName'];
	userManager.findUserByName(userName, function(user) {
		if (user) {
			res.json(user);
		} else {
			user = new userManager.UserModel({
				name : userName,
				currentGames : [],
				xp : 0,
				wins : 0,
				losses : 0,
				coins : 0,
				usedCoins : -1,
				watchedAd : false
			});
			rankManager.findRankByName("1", function(dbRank) {
				user.rankInfo = dbRank;
				user.save(function() {
					res.json(user);
				});
			});
		}
	});
}

exports.requestHandleForUserName = function(req, res) {
	var userName = req.body['userName'];
	var handle = req.body['handle'];

	var updatedHandle = handle.replace(/^\s+/, '');
	updatedHandle = updatedHandle.replace(/\s+$/, '');
	updatedHandle = updatedHandle.replace(/[^a-z0-9_]/i);

	if (handle.length < 3 || handle.length > 16 || updatedHandle !== handle) {
		res.json({
			created : false,
			reason : "Invalid username"
		});
	} else {
		var p = userManager.findUserByHandle(handle);
		p.then(function(user) {
			if (user) {
				res.json({
					created : false,
					reason : "Username already chosen by another player"
				});
			} else {
				userManager.findUserByName(userName, function(user) {
					user.handle = handle;
					user.save(function() {
						res.json({
							created : true,
							player : user
						})
					});
				});
			}
		}).then(null, logErrorAndSetResponse(req, res));
	}
}

exports.findCurrentGamesByPlayerHandle = function(req, res) {
	var playerHandle = req.query['playerHandle'];
	var p = userManager.findUserByHandle(playerHandle);
	p.then(function(user) {
		if (!user) {
			res.json({});
		} else {
			gameManager.findCollectionOfGames(user.currentGames, function(games) {
				var returnObj = {};
				returnObj.items = games;
				res.json(returnObj);
			});
		}
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.performMoves = function(req, res) {
	var gameId = req.body.id;
	var moves = req.body.moves;
	var playerHandle = req.body.playerHandle;

	gameManager.performMoves(gameId, moves, playerHandle, 0, function(savedGame) {
		if (!savedGame) {
			res.json({
				error : "Could not perform move, please try again"
			});
		} else {
			if (savedGame.endGameInformation.winnerHandle) {
				var p = userManager.findUserByHandle(savedGame.endGameInformation.winnerHandle);
				p.then(function(user) {
					user.wins++;
					user.xp += 10;
					savedGame.endGameInformation.xpAwardToWinner = 10;
					rankManager.findRankForXp(user.xp, function(rank) {
						user.rankInfo = rank;
						user.save(function() {
							res.json(processGameReturn(savedGame, playerHandle));
						});
					});
				});
			} else {
				res.json(processGameReturn(savedGame, playerHandle));
			}
		}
	});
}

processGameReturn = function(game, playerWhoCalledTheUrl) {

	for ( var i = 0; i < game.moves.length; i++) {
		var move = game.moves[i];

		if ((move.playerHandle == playerWhoCalledTheUrl) && move.startingRound == game.currentRound.roundNumber) {
			decrementPlanetShipNumber(game, move);
		}
	}

	return game;
}

decrementPlanetShipNumber = function(game, move) {
	for ( var i = 0; i < game.planets.length; i++) {
		var planet = game.planets[i];
		if (planet.name == move.fromPlanet) {
			planet.numberOfShips = planet.numberOfShips - move.fleet;
		}
	}
}

exports.joinGame = function(req, res) {
	var gameId = req.query['id'];
	var playerHandle = req.query['playerHandle'];

	var p = userManager.findUserByHandle(playerHandle);
	p.then(function(user) {
		gameManager.addUser(gameId, user, function(game) {
			gameManager.findById(gameId, function(returnGame) {
				user.currentGames.push(gameId);
				user.save(function() {
					res.json(returnGame);
				});
			});
		});
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.addCoins = function(req, res) {
	var playerHandle = req.body.playerHandle;
	var numCoins = req.body.numCoins;
	var usedCoins = req.body.usedCoins;
	
	var p  = userManager.addCoins(numCoins, playerHandle, usedCoins);
	p.then(handleUserUpdate(req, res, playerHandle), logErrorAndSetResponse(req, res));	
}

exports.addCoinsForAnOrder = function(req, res) {
	var playerHandle = req.body.playerHandle;
	var numCoins = req.body.numCoins;
	var usedCoins = req.body.usedCoins;
	var orders = req.body.orders;
	
	if(orders && orders.length > 0){
		var p  = userManager.addCoinsForAnOrder(numCoins, playerHandle, usedCoins, orders[0]);
		p.then(handleUserUpdate(req, res, playerHandle), logErrorAndSetResponse(req, res));
	}else{
		var userReturnInfo = handleUserUpdate(req, res, playerHandle);
		userReturnInfo(null);
	}
	
	
	
}

var handleUserUpdate = function(req, res, handle){
	return function(user){
		if(user === null){
			var findUserPromise = userManager.findUserByHandle(handle);
			findUserPromise.then(function(found){
				res.json(found);
			}, logErrorAndSetResponse(req, res));
		}else{
			res.json(user);
		}
	}
}


exports.reduceTimeUntilNextGame = function(req, res) {
	var handle = req.body.playerHandle;
	var usedCoins = req.body.usedCoins;
	var timeRemaining = req.body.timeRemaining;

	configManager.findLatestConfig("payment", function(config) {

		var p = userManager.reduceTimeForWatchingAd(handle, usedCoins, timeRemaining, config.values['timeReduction']);
		p.then(handleUserUpdate(req, res, handle), logErrorAndSetResponse(req, res));

	});
}

exports.findConfigByType = function(req, res) {
	var type = req.query['type'];

	var p = configManager.findLatestConfig(type);
	p.then(function(config) {
		res.json(config);
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.findRankInformation = function(req, res) {
	var rank = req.query['rank'];
	rankManager.findRankByName(rank, function(dbRank) {
		res.json(dbRank);
	});
}

exports.findAllMaps = function(req, res) {
	mapManager.findAllMaps(function(maps) {
		var returnObj = {};
		returnObj.items = maps;
		res.json(returnObj);
	});
}

exports.matchPlayerToGame = function(req, res) {
	var mapToFind = req.body.mapToFind;
	var playerHandle = req.body.playerHandle;
	var time = req.body.time;

	var p = userManager.findUserByHandle(playerHandle);
	p.then(function(user) {
		return findOrCreateGamePromise(user, time, mapToFind);
	}).then(function(game) { res.json(game); }, logErrorAndSetResponse(req, res));
}

var findOrCreateGamePromise = function(user, time, mapToFind) {
	var p = gameManager.findGameForMapInTimeLimit(mapToFind, time - 300000, user.handle);
	return p.then(function(games) {
		var joinP = joinGamePromise(games, user, time);
		return joinP.then(function(game) {
			if(game) {
				return game;
			}
			var gameP = gameManager.findGameAtAMap(mapToFind, user.handle);
			return gameP.then(function(games) {
				var innerJoinP = joinGamePromise(games, user, time);
				return innerJoinP.then(function(game) {
					if(game) {
						return game;
					} 
					return generateGamePromise(user, time, mapToFind);
				});
			});
		});
	});
}

var joinGamePromise = function(games, user, time) {
	if(games.length < 1) {
		var p = new mongoose.Promise();
		p.complete(null);
		return p;
	}
	
	var joinP = attemptToJoinGamePromise(games, user, time);	
	return joinP.then(function(game) {
		return game;
	});
}

var logErrorAndSetResponse = function(req, res) {
	return function(err) {
		console.log(req.connection.remoteAddress + " " + err.stack);
		res.json({
			"error" : err
		});
	}
}

var attemptToJoinGamePromise = function(games, user, time) {
	var gamesByRelativeRank = _.sortBy(games, function(game) {
		return Math.abs(user.rankInfo.level - game.rankOfInitialPlayer);
	});

	return addGameFromSegmentPromise(gamesByRelativeRank, 0, user, time);
}

var addGameFromSegmentPromise = function(games, index, user, time) {
	if (index >= games.lengh) {
		return null;
	}

	var gameId = games[index]._id;

	return gameManager.addUser(gameId, user).then(function(game) {
		if (game !== null) {
			return gameManager.findById(gameId).then(function(returnGame) {
				decrementCoins(user, gameId, time);
				var p = withPromise(user.save, user);
				return p.then(function() {
					return returnGame;
				});
			});
		} else {
			return addGameFromSegmentPromise(games, index + 1, user, time);
		}
	});
}

var decrementCoins = function(user, gameId, time) {
	user.currentGames.push(gameId);
	user.coins--;
	if (user.coins == 0) {
		user.usedCoins = time;
	}
}

var generateGamePromise = function(user, time, mapToFind) {
	var p = mapManager.findMapByKey(mapToFind);
	return p.then(function(map) {
		var widthToUse = Math.floor(Math.random() * (map.width.max - map.width.min + 1)) + map.width.min;
		var heightToUse = Math.ceil(widthToUse * 1.33);

		var numPlanets = Math.floor((widthToUse * heightToUse) * .28);
		numPlanets = Math.max(12, numPlanets);

		var gameTypeIndex = Math.floor(Math.random() * (map.gameType.length));

		var gameAttributes = {
			players : [ user ],
			width : widthToUse,
			height : heightToUse,
			numberOfPlanets : numPlanets,
			createdTime : time,
			rankOfInitialPlayer : user.rankInfo.level,
			map : map.key,
			gameType : map.gameType[gameTypeIndex]
		};

		return gameManager.createGame(gameAttributes).then(function(game) {
			return gameManager.saveGame(game);
		}).then(function(game) {
			decrementCoins(user, game.id, time);
			
			var p = withPromise(user.save, user);
			return p.then(function() {
				return game;
			});
		});
	});
}

var withPromise = function(func, obj) {
	var p = new mongoose.Promise();
	p.complete();
	var array = Array.prototype.slice.call(arguments, 1);
	array.push(function(err, result) {
		if(err) {
			p.reject(err);
		} else {
			p.complete(result);
		}
	});
	func.apply(obj, array);
	return p;
}


exports.findAllInventory = function(req, res){
	var p = inventoryManager.InventoryModel.find().exec();
	p.then(function(inventory){
		res.json({items : inventory});
	}).then(null, logErrorAndSetResponse(req, res));
};

exports.deleteConsumedOrders = function(req, res){
	var playerHandle = req.body.playerHandle;
	var orders = req.body.orders;
	
	if(orders && orders.length > 0){
		var p  = userManager.deleteConsumedOrder(playerHandle, orders[0]);
		p.then(handleUserUpdate(req, res, playerHandle), logErrorAndSetResponse(req, res));
	}else{
		var userReturnInfo = handleUserUpdate(req, res, playerHandle);
		userReturnInfo(null);
	}
	
	
}

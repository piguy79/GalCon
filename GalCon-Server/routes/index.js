var mongoose = require('../modules/model/mongooseConnection').mongoose,
	gameBuilder = require('../modules/gameBuilder'), 
	gameManager = require('../modules/model/game'), 
	userManager = require('../modules/model/user'), 
	rankManager = require('../modules/model/rank'), 
	mapManager = require('../modules/model/map'), 
	configManager = require('../modules/model/config'), 
	leaderboardManager = require('../modules/model/leaderboard'), 
	inventoryManager = require('../modules/model/inventory'), 
	_ = require('underscore'),
	socialManager = require('../modules/social'),
	validation = require('../modules/validation'),
	googleapis = require('googleapis');

var VALIDATE_MAP = {
	email : validation.isEmail,
	session : validation.isSession,
	handle : validation.isHandle,
	mapKey : validation.isMapKey,
	orders : validation.isOrders
};

exports.index = function(req, res) {
	res.render('index.html')
};

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
	var p = gameManager.findAvailableGames(playerHandle);
	p.then(function(games) {
		var returnObj = {};
		returnObj.items = games;
		res.json(returnObj);
	});
}

exports.findGamesWithPendingMove = function(req, res) {
	var handle = req.query['handle'];
	var session = req.query['session'];
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function() {
		var p = userManager.findUserByHandle(handle);
		return p.then(function(user) {
			gameManager.findCollectionOfGames(user.currentGames, function(games) {
				var len = games.length;
				while (len--) {
					if (games[len].currentRound.playersWhoMoved.indexOf(user.handle) >= 0
							|| games[len].endGameInformation.winnerHandle) {
						games.splice(len, 1);
					}
				}
				var minifiedGames = minfiyGameResponse(games, user.handle);
				res.json({items : minifiedGames});
			});
		});
	}).then(null, logErrorAndSetResponse(req, res));
}

var validate = function(propMap, res) {
	for(key in propMap) {
		if(!VALIDATE_MAP[key].call(this, propMap[key])) {
			console.log("Invalid " + key + " detected: " + propMap[key]);
			res.json({ valid : false, reason : "Invalid " + key });
			return false;
		}	
	}
	
	return true;
}
	
var validateSession = function(session, emailOrHandleMap) {
	var key = _.keys(emailOrHandleMap)[0];
	var value = _.values(emailOrHandleMap)[0];
	
	var p = getSessionState(session, key, value);
	return p.then(function(state) {
		if(state === "NOT FOUND") {
			throw new ErrorWithResponse("Invalid session detected for: " + session + ", " + value, { session : "invalid"});
		} else if(state === "EXPIRED SESSION") {
			var obj = {};
			obj[key] = value;
			var invalidP = userManager.UserModel.findOneAndUpdate(obj, {$set : {session : {}}}).exec();
			return invalidP.then(function() {
				throw new ErrorWithResponse("Expired session", { session : "expired" });
			});
		}
	});
}

var getSessionState = function(session, key, value) {
	var query = {'session.id' : session};
	query[key] = value;

	var p = userManager.UserModel.findOne(query).exec();
	return p.then(function(user) {
		if(user === null) {
			return "NOT FOUND";
		} else if(user.session.expireDate.getTime() < Date.now()) {
			return "EXPIRED SESSION";
		}
		
		return "GOOD";
	});
}

exports.findUserByEmail = function(req, res) {
	var email = req.query['email'];
	var session = req.query['session'];
	
	if(!validate({email : email, session : session}, res)) {
		return;
	}
	
	var p = validateSession(session, {"email" : email});
	p.then(function() {
		var validP = userManager.findUserByEmail(email);
		return validP.then(function(user) {
			if (user) {
				res.json(user);
			} else {
				res.json({});
			}
		});
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.requestHandleForEmail = function(req, res) {
	var session = req.body['session'];
	var email = req.body['email'];
	var handle = req.body['handle'];
	
	if(!validate({email : email, session : session, handle : handle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"email" : email});
	p.then(function() {
		var validP = userManager.findUserByHandle(handle);
		return validP.then(function(user) {
			if (user) {
				res.json({
					created : false,
					reason : "Username already chosen"
				});
			} else {
				var innerp = userManager.findUserByEmail(email);
				return innerp.then(function(user) {
					if(user === null) {
						console.error("Attempted to create handle for invalid email: " + email);
						return null;
					} else if(user.handle !== undefined) {
						res.json({ created : false, reason : "Cannot change handle" });
					}
					user.handle = handle;
					return user.withPromise(user.save);
				}).then(function(user) {
					if(user === null) {
						res.json({ created : false, reason : "Invalid username" });
					} else {
						res.json({ created : true, player : user });
					}
				});
			}
		});
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.findCurrentGamesByPlayerHandle = function(req, res) {
	var session = req.query['session'];
	var handle = req.query['handle'];
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function() {
		var p = userManager.findUserByHandle(handle);
		return p.then(function(user) {
			if(!user) {
				throw new Error("Could not find user object for handle: " + handle);
			}
			return gameManager.findCollectionOfGames(user.currentGames);
		}).then(function(games) {
			var minifiedGames = minfiyGameResponse(games, handle);
			res.json({items : minifiedGames});
		});
	}).then(null, logErrorAndSetResponse(req, res));
}

var minfiyGameResponse = function(games, handle){
	return _.map(games, function(game){
		var iHaveAMove = _.filter(game.currentRound.playersWhoMoved, function(player) { return player === handle}).length === 0;	
		return {
			id : game._id,
			players : _.map(game.players, function(player) { return { handle : player.handle, rank : player.rankInfo.level}}),
			createdDate : game.createdDate,
			moveAvailable : iHaveAMove,
			winner : game.endGameInformation.winnerHandle,
			winningDate : game.endGameInformation.winningDate,
			map : game.map
		};
	});
}

exports.performMoves = function(req, res) {
	var gameId = req.body.id;
	var moves = req.body.moves;
	var playerHandle = req.body.playerHandle;
	var time = req.body.time;
	var harvest = req.body.harvest;

	var p = gameManager.performMoves(gameId, moves, playerHandle, 0, harvest);
	p.then(function(game) {
		if (!game) {
			res.json({
				error : "Could not perform move, please try again"
			});
		} else {
			var p = new mongoose.Promise();
			p.complete();
			
			return p.then(function() {
				if (game.endGameInformation.winnerHandle) {
					return updateWinnersAndLosers(game, time);
				}
				return game;
			}).then(function(gameToReturn) {
				res.json(processGameReturn(gameToReturn, playerHandle));
			});
		}
	}).then(null, logErrorAndSetResponse(req, res));
}

var updateWinnersAndLosers = function(game, time) {
	var winner;
	
	var p = new mongoose.Promise();
	p.complete();
	
	game.players.forEach(function(player) {
		p = p.then(function() {
			if(player.handle === game.endGameInformation.winnerHandle) {
				winner = player;
				player.wins += 1;
				player.xp += 10;
				game.endGameInformation.xpAwardToWinner = 10;
			} else {
				player.losses += 1;
			}
			var coinPromise = updateUserTime(player, time, game._id);
			return coinPromise.then(function(user){
				player.usedCoins = user.usedCoins;
				return player.withPromise(player.save);
			});
		});
	});
	
	return p.then(function() {
		return rankManager.findRankForXp(winner.xp);
	}).then(function(rank) {
		winner.rankInfo = rank;
		winner.withPromise(winner.save);
	}).then(function() {
		return game;
	});
}

var updateUserTime = function(user, time, gameId){
	var p = new mongoose.Promise();
	var userPromise = userManager.findUserWithGames(user.handle);
	userPromise.then(function(foundUser){
		
		var gamesStillInProgress = _.filter(foundUser.currentGames, function(game){ return game._id !== gameId && game.endGameInformation.winnerHandle === ''});
		
		if(foundUser.coins <= 0 && gamesStillInProgress.length === 0){
			user.usedCoins = time;
		}
		
		p.complete(user);
	});
	
	return p;
}

exports.adjustUsedCoinsIfAllUserGamesAreComplete = function(req, res) {
	var handle = req.body.handle;
	var session = req.body.session;
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function() {
		var userPromise = userManager.findUserWithGames(handle);
		return userPromise.then(function(user){
			var gamesStillInProgress = _.filter(user.currentGames, function(game) { return game.endGameInformation.winnerHandle === ''});
		
			if(user.usedCoins === -1 && gamesStillInProgress.length === 0 && user.coins === 0) {
				user.usedCoins = Date.now();
			}
		
			return user.withPromise(user.save);
		}).then(function(user) {
			res.json(user);
		});
	}, logErrorAndSetResponse(req, res));
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

	var user;
	var game;
	var p = userManager.findUserByHandle(playerHandle);
	p.then(function(foundUser) {
		user = foundUser;
		return gameManager.addUser(gameId, user);
	}).then(function(savedGame) {
		if(savedGame == null) {
			console.log("Could not join game. Perhaps someone already joined?");
			res.json({
				"error" : "Could not join game"
			})
		}
		game = savedGame;
		user.currentGames.push(game);
		return user.withPromise(user.save);
	}).then(function() {
		res.json(game);
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.addFreeCoins = function(req, res) {
	var handle = req.body.handle;
	var session = req.body.session;
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function() {
		var p = userManager.findUserByHandle(handle);
		return p.then(function(user) {
			var innerp = configManager.findLatestConfig('app');
			return innerp.then(function(config) {
				var delay = config.values['timeLapseForNewCoins'];
				if(user.coins === 0 && user.usedCoins !== -1 && user.usedCoins + delay < Date.now()) {
					return userManager.addCoins(config.values['freeCoins'], handle);
				} else {
					return user;
				}
			}).then(function(user) {
				res.json(user);
			})
		});
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.addCoinsForAnOrder = function(req, res) {
	var handle = req.body.handle;
	var orders = req.body.orders;
	var session = req.body.session;
	
	if(!validate({session : session, handle : handle, orders : orders}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function() {
		if(orders && orders.length > 0) {
			var gapiP = new mongoose.Promise();
			googleapis
				.discover('androidpublisher', 'v1.1')
				.execute(function(err, client) {
					if(err) {
						gapiP.reject(err.message);
					} else {
						gapiP.complete(client);
					}
				});
			var lastP = gapiP;
			_.each(orders, function(order) {
				lastP = lastP.then(function(client) {
					var newP = new mongoose.Promise();
					var clientId = "1066768766862-6nqm53i5ab34js3oo8jcv05bkookob87.apps.googleusercontent.com";
					var clientSecret = "2R4_ZmTBwBEAmEo7_W8hIyZn";
					var oAuthClient = new googleapis.OAuth2Client(clientId, clientSecret);
					oAuthClient.setCredentials({
						refresh_token: "1/Cw4H-MslYOEbjtjfkAEM6oOBaRGS4GZIu4Rl5jCJ9So",
						access_token: "ya29.1.AADtN_W_-u9YM-kof2kK1nnryrUIQuAgNR_iRwmP9JwNcYaRzcr4uvSQqgFdkg"
					});
					client.androidpublisher.inapppurchases
						.get({
							packageName: order.packageName,
							productId: order.productId,
							token: order.token
							})
						.withAuthClient(oAuthClient)
						.execute(function(err, result) {
							if(err) {
								console.log("Android Publisher API - Error - %j", err);
								newP.reject(err.message);
							} else {
								console.log("Android Publisher API - Result - %j", result);
								if(result.purchaseState == 0 && result.consumptionState == 1) {
									newP.complete("credit");
								} else {
									newP.complete("noCredit");
								}
							}
						});
					return newP;
				}).then(function(validationResult) {
					if(validationResult === "credit") {
						return userManager.addCoinsForAnOrder(handle, order);
					} else {
						return userManager.findUserByHandle(handle);
					}
				});
			});
			return lastP.then(function(user) {
				res.json(user);
			}, logErrorAndSetResponse(req, res));
		} else {
			var userReturnInfo = handleUserUpdate(req, res, handle);
			userReturnInfo(null);
		}
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.deleteConsumedOrders = function(req, res){
	var playerHandle = req.body.playerHandle;
	var orders = req.body.orders;
	
	if(orders && orders.length >0){
		var lastPromise = performFunctionToOrders(userManager.deleteConsumedOrder, orders, playerHandle);
		lastPromise.then(handleUserUpdate(req, res, playerHandle), logErrorAndSetResponse(req, res));
	}else{
		var userReturnInfo = handleUserUpdate(req, res, playerHandle);
		userReturnInfo(null);
	}
}

var performFunctionToOrders = function(func, objects){
	var promise = new mongoose.Promise();
	promise.complete();
	var lastPromise = promise;
	var mainArgs = arguments;
	
	objects.forEach(function(object){
		lastPromise = lastPromise.then(function(){
			var args = Array.prototype.slice.call(mainArgs, 2);
			args.push(_.extend({},object));
			return func.apply(this, args);
		});
	});
	
	return lastPromise
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
	var handle = req.body.handle;
	var session = req.body.session;
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function() {
		var innerp = configManager.findLatestConfig('app');
		return innerp.then(function(config){
			return userManager.reduceTimeForWatchingAd(handle, config);
		}).then(handleUserUpdate(req, res, handle));
	}).then(null, logErrorAndSetResponse(req, res));
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
	var handle = req.body.handle;
	var session = req.body.session;
	
	if(!validate({session : session, handle : handle, mapKey : mapToFind}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function() {
		var p = userManager.findUserByHandle(handle);
		return p.then(function(user) {
			if(user.coins < 1) {
				throw new Error(user.handle + " attempted to start game with " + user.coins + " coins");
			}
			return findOrCreateGamePromise(user, Date.now(), mapToFind);
		}).then(function(game) { 
			res.json(game);
		});
	}).then(null, logErrorAndSetResponse(req, res));
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

function ErrorWithResponse(message, responseObj) {
	this.name = "ErrorWithReponse";
	this.message = message;
	this.responseObj = responseObj;
}

ErrorWithResponse.prototype = new Error();
ErrorWithResponse.prototype.constructor = ErrorWithResponse;

var logErrorAndSetResponse = function(req, res) {
	return function(err) {
		if(typeof err === "string") {
			console.log(req.connection.remoteAddress + " " + err);
		} else if(err instanceof ErrorWithResponse) {
			console.log(req.connection.remoteAddress + " " + err.message);
		} else {
			console.log(req.connection.remoteAddress + " " + err.stack);
		}
		
		if(err instanceof ErrorWithResponse) {
			res.json(err.responseObj);
		} else {
			res.json({
				"error" : err
			});
		}
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
				user.currentGames.push(game);
				user.coins--;
				var p = user.withPromise(user.save);
				return p.then(function() {
					return returnGame;
				});
			});
		} else {
			return addGameFromSegmentPromise(games, index + 1, user, time);
		}
	});
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
			return game.withPromise(game.save);
		}).then(function(game) {
			user.currentGames.push(game);
			user.coins--;
			
			var p = user.withPromise(user.save);
			return p.then(function() {
				return game;
			});
		});
	});
}

exports.findAllInventory = function(req, res){
	var p = inventoryManager.InventoryModel.find().exec();
	p.then(function(inventory){
		res.json({items : inventory});
	}).then(null, logErrorAndSetResponse(req, res));
};

exports.exchangeToken = function(req, res) {
	var authProvider = req.body.authProvider;
	var token = req.body.token;

	var p = socialManager.exchangeToken(authProvider, token);
	p.then(function(session) {
		res.json({session : session});
	}).then(null, logErrorAndSetResponse(req, res));
}

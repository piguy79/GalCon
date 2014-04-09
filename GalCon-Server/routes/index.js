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
	moveValidation = require('../modules/move_validation'),
	inviteValidation = require('../modules/invite_validation'),
	claimValidation = require('../modules/claim_validation'),
	googleapis = require('googleapis')
	,ObjectId = require('mongoose').Types.ObjectId;

var VALIDATE_MAP = {
	email : validation.isEmail,
	session : validation.isSession,
	handle : validation.isHandle,
	mapKey : validation.isMapKey,
	orders : validation.isOrders,
	move : validation.isValidMoves,
	gameId : validation.isGameId
};

exports.index = function(req, res) {
	res.render('index.html')
};

exports.searchUsers = function(req, res){
	var searchTerm = req.query['searchTerm'];
	var session = req.query['session'];
	var handle = req.query['handle'];
	
	if(!validate({handle : searchTerm, session : session}, res)) {
		return;
	}
	
	var p = userManager.findUserMatchingSearch(searchTerm, handle);
	p.then(function(people) {
		res.json({items : _.map(people, minifyUser)});
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.findGameById = function(req, res) {
	var searchId = req.query['id'];
	var handle = req.query['handle'];
	
	var p = gameManager.findById(searchId);
	p.then(function(game) {
		res.json(processGameReturn(game, handle));
	}).then(null, logErrorAndSetResponse(req, res));
}


exports.findGamesWithPendingMove = function(req, res) {
	var handle = req.query['handle'];
	
	if(!validate({handle : handle}, res)) {
		return;
	}
	
	var pendingMoveCount;
	
	var p = userManager.findUserByHandle(handle);
	return p.then(function(user) {
		if(user) {
			return gameManager.findCollectionOfGames(user);
		}
		return null;
	}).then(function(games) {
		if(games === null) {
			return 0;
		}
		var count = 0;
		for(i in games) {
			if (games[i].round.moved.indexOf(handle) == -1
					&& !games[i].endGame.winnerHandle && !games[i].endGame.declined) {
				count += 1;
			}
		}
		return count;
	}).then(function(count){
		pendingMoveCount = count;
		return gameManager.findByInvitee(handle);
	}).then(function(invites){
		if(invites === null){
			return 0;
		}else{
			return invites.length;
		}
	}).then(function(inviteCount) {
		
		res.json({c : pendingMoveCount, i : inviteCount}); 
	}, logErrorAndSetResponse(req, res));
}

var validate = function(propMap, res) {
	for(key in propMap) {
		if(!VALIDATE_MAP[key].call(this, propMap[key])) {
			console.log("Invalid " + key + " detected: %j", propMap[key]);
			res.json({ valid : false, reason : "Invalid " + key });
			return false;
		}	
	}
	
	return true;
}
	
var validateSession = function(session, authIdOrHandleMap) {
	var key = _.keys(authIdOrHandleMap)[0];
	var value = _.values(authIdOrHandleMap)[0];
		
	var p = getSessionState(session, key, value);
	return p.then(function(state) {
		var msg;
		if(state === "NOT FOUND") {
			msg = "Invalid session detected for: " + session + ", " + value;
		} else if(state === "EXPIRED SESSION") {
			msg = "Expired session";
		}
		
		if(msg !== undefined) {
			var obj = {};
			obj[key] = value;
			var invalidP = userManager.UserModel.findOneAndUpdate(obj, {$set : {session : {}}}).exec();
			return invalidP.then(function(user) {
				console.log(msg);
				throw new ErrorWithResponse(msg, { session : "expired" });
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

exports.findUserById = function(req, res) {
	var id = req.query['id'];
	var authProvider = req.query['authProvider']
	var session = req.query['session'];
	
	if(!validate({session : session}, res)) {
		return;
	}
	var key = 'auth.' + authProvider;
	var query= {};
	query[key] = id;
	var p = validateSession(session, query);
	p.then(function() {
		var validP = userManager.findUserById(id, authProvider);
		return validP.then(function(user) {
			if (user) {
				res.json(user);
			} else {
				res.json({});
			}
		});
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.requestHandleForId = function(req, res) {
	var session = req.body['session'];
	var id = req.body['id'];
	var authProvider = req.body['authProvider'];
	var handle = req.body['handle'];
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var key = "auth." + authProvider;
	var search = {};
	search[key] = id;
	var p = validateSession(session, search);
	p.then(function() {
		var validP = userManager.findUserByHandle(handle);
		return validP.then(function(user) {
			if (user) {
				res.json({
					created : false,
					reason : "Username already chosen"
				});
			} else {
				var innerp = userManager.findUserById(id, authProvider);
				return innerp.then(function(user) {
					if(user === null) {
						console.error("Attempted to create handle for invalid id: " + email);
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
			return gameManager.findCollectionOfGames(user);
		}).then(function(games) {
			var minifiedGames = minfiyGameResponse(games, handle);
			res.json({items : minifiedGames});
		});
	}).then(null, logErrorAndSetResponse(req, res));
}

var minfiyGameResponse = function(games, handle){
	return _.map(games, function(game){
		var iHaveAMove = _.filter(game.round.moved, function(player) { return player === handle}).length === 0;	
		var claimAvailable = claimValidation.validateClaim(game, handle);
		return {
			id : game._id,
			players : _.map(game.players, minifyUser),
			createdDate : game.createdDate,
			moveAvailable : iHaveAMove,
			winner : game.endGame.winnerHandle,
			date : game.endGame.date,
			map : game.map,
			social : game.social,
			claimAvailable : claimAvailable
		};
	});
}

exports.performMoves = function(req, res) {
	var gameId = req.body.id;
	var moves = req.body.moves;
	var playerHandle = req.body.playerHandle;
	var harvest = req.body.harvest;
	var session = req.body.session;
	
	var finalGameToReturn;
	
	if(!validate({move : {moves : moves, handle : playerHandle}, session : session, handle : playerHandle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : playerHandle});
	p.then(function(){
		return moveValidation.validate(gameId, playerHandle, moves);
	}).then(function(result){
		if(!result.success){
			res.json({ valid : false, reason : "Invalid move" });
		}else{
			return gameManager.performMoves(gameId, moves, playerHandle, 0, harvest);
		}
	}).then(function(game) {
		if (!game) {
			res.json({
				error : "Could not perform move, please try again"
			});
		} else {
			var p = new mongoose.Promise();
			p.complete();
			
			return p.then(function() {
				if (game.endGame.winnerHandle) {
					return updateWinnersAndLosers(game);
				}
				return game;
			}).then(function(gameToReturn){
				res.json(processGameReturn(gameToReturn, playerHandle));
			});
		}
	}).then(null, logErrorAndSetResponse(req, res));
}

var updateWinnersAndLosers = function(game) {
	var winner;
	
	var p = new mongoose.Promise();
	p.complete();
	
	game.players.forEach(function(player) {
		p = p.then(function() {
			if(player.handle === game.endGame.winnerHandle) {
				winner = player;
				player.wins += 1;
				player.xp += parseInt(game.config.values["xpForWinning"]);
				game.endGame.xp = parseInt(game.config.values["xpForWinning"]);
			} else {
				player.losses += 1;
			}
			var coinPromise = setTimeUntilFreeCoins(player, game._id);
			return coinPromise.then(function(user){
				player.usedCoins = user.usedCoins;
				return player.withPromise(player.save);
			});
		});
	});
	
	return p.then(function() {
		return game.withPromise(game.save);
	}).then(function(updatedGame) {
		return updatedGame;
	});
}

var setTimeUntilFreeCoins = function(user, gameId){
	var p = gameManager.findCollectionOfGames(user);
	return p.then(function(games){	
		var gamesStillInProgress = _.filter(games, function(game){ return game._id !== gameId && game.endGame.winnerHandle === ''});
		
		if(user.coins <= 0 && gamesStillInProgress.length === 0){
			user.usedCoins = Date.now();
		}
		
		return user;
	});
}

exports.adjustUsedCoinsIfAllUserGamesAreComplete = function(req, res) {
	var handle = req.body.handle;
	var session = req.body.session;
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function() {
		var userPromise = userManager.findUserByHandle(handle);
		var foundUser;
		return userPromise.then(function(user){
			foundUser = user;
			return gameManager.findCollectionOfGames(user);
			
		}).then(function(games){
			var gamesStillInProgress = _.filter(games, function(game) { return game.endGame.winnerHandle === ''});
			
			if(foundUser.usedCoins === -1 && gamesStillInProgress.length === 0 && foundUser.coins === 0) {
				foundUser.usedCoins = Date.now();
			}
		
			return foundUser.withPromise(foundUser.save);
		}).then(function(user) {
			res.json(user);
		});
	}, logErrorAndSetResponse(req, res));
}


processGameReturn = function(game, playerWhoCalledTheUrl) {
	for (var i = 0; i < game.moves.length; i++) {
		var move = game.moves[i];

		if ((move.handle == playerWhoCalledTheUrl) && move.startingRound == game.round.num) {
			decrementPlanetShipNumber(game, move);
		}
	}
	
	_.each(game.planets, function(planet) {
		if(planet.harvest && planet.harvest.startingRound === game.round.num && planet.handle !== playerWhoCalledTheUrl) {
			planet.harvest = undefined;
		}
	});
	
	game.moves = _.reject(game.moves, function(move){
		return move.handle !== playerWhoCalledTheUrl && move.executed === false;
	});
	

	return game;
}

decrementPlanetShipNumber = function(game, move) {
	for ( var i = 0; i < game.planets.length; i++) {
		var planet = game.planets[i];
		if (planet.name == move.from) {
			planet.ships = planet.ships - move.fleet;
		}
	}
}

exports.joinGame = function(req, res) {
	var gameId = req.query['id'];
	var handle = req.query['handle'];

	var user;
	var game;
	var p = userManager.findUserByHandle(handle);
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
		return user.withPromise(user.save);
	}).then(function() {
		res.json(processGameReturn(game, handle));
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.acceptInvite = function(req, res){
	var gameId = req.query['gameId'];
	var handle = req.query['handle'];
	var session = req.query['session'];
	
	if(!validate({session : session, handle : handle, gameId : gameId}, res)) {
		return;
	}
	
	var currentUser;
	var currentGame;
	var requestingUser;
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function(){
		return userManager.findUserByHandle(handle);
	}).then(function(user){
		currentUser = user;
		if(currentUser.coins < 1){
			throw new Error(handle + " attempted to join a game with insufficient coins.");
		}
		return gameManager.GameModel.findOne({_id : gameId}).populate('players').exec();
	}).then(function(game){
		if(game === null){
			throw new Error("This game does not exist.");
		}
		requestingUser = game.players[0];
		return gameManager.addSocialUser(gameId, currentUser);
	}).then(function(savedGame){
		currentGame = savedGame;
		return userManager.joinAGame(currentUser, savedGame);
	}).then(function(user){
		if(!user){
			throw new Error('Unable to join the game');
		}else{
			return userManager.updateFriend(currentUser, requestingUser);
		}
	}).then(function(){
		res.json(processGameReturn(currentGame, handle))
	}).then(null, logErrorAndSetResponse(req, res));
}


exports.declineInvite = function(req, res){
	var gameId = req.query['gameId'];
	var handle = req.query['handle'];
	var session = req.query['session'];
	
	if(!validate({session : session, handle : handle, gameId : gameId}, res)) {
		return;
	}
	
	var requester;
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function(){
		return gameManager.GameModel.findOne({_id : gameId}).populate('players').exec();
	}).then(function(game){
		if(game.social.invitee === handle){
			requester = game.players[0];
			return userManager.removeAGame(requester, gameId);
		}else{
			throw new Error("User" + handle +  " cannot decline game.");
		}
	}).then(function(){
		return gameManager.declineSocialGame(gameId, handle);
	}).then(function(){
		res.json({success : true});
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.resignGame = function(req, res) {
	var handle = req.body.handle;
	var session = req.body.session;
	var gameId = req.params.id;
	
	if(!validate({session : session, handle : handle, gameId : gameId}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function() {
		var p1 = gameManager.findById(gameId);
		return p1.then(function(game) {
			if(game === null) {
				throw new Error("Invalid game");
			}
			
			var foundHandle = false;
			game.players.forEach(function(player) {
				if(player.handle !== handle) {
					game.endGame.winnerHandle = player.handle;
				} else {
					foundHandle = true;
				}
			});
			if(!foundHandle) {
				throw new Error("Invalid game");
			}
			game.endGame.date = Date.now();
			
			return updateWinnersAndLosers(game);
		});
	}).then(function(game) {res.json(game);}, logErrorAndSetResponse(req, res));
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
	
	var configReturn;

	var p = configManager.findLatestConfig(type);
	p.then(function(config) {
		configReturn = config;
		return rankManager.findAllRanks();
	}).then(function(ranks){
		res.json({config : configReturn, ranks : ranks});
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
	
	var callingUser;
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function(){
		return userManager.findUserByHandle(handle);
	}).then(function(user){
		callingUser = user;
		return mapManager.findMapByKey(mapToFind);
	}).then(function(map){
		if(callingUser.xp < map.availableFromXp){
			throw new Error(handle + " does not have access to this map.");
		}
		var p = userManager.findUserByHandle(handle);
		return p.then(function(user) {
			if(user.coins < 1) {
				throw new Error(user.handle + " attempted to start game with " + user.coins + " coins");
			}
			return findOrCreateGamePromise(user, Date.now(), mapToFind);
		}).then(function(game) { 
			res.json(processGameReturn(game, handle));
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
					return generateGamePromise([user], time, mapToFind);
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
	
	var joinP = rankManager.findAllRanks();
	return joinP.then(function(ranks){
		var rankOfUser = rankManager.findRankForAnXp(ranks, user.xp);
		return 	attemptToJoinGamePromise(games, user, time, rankOfUser);	
	}).then(function(game) {
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
				"error" : err.message
			});
		}
	}
}

var attemptToJoinGamePromise = function(games, user, time, rankOfUser) {
	var gamesByRelativeRank = _.sortBy(games, function(game) {
		return Math.abs(rankOfUser.level - game.rankOfInitialPlayer);
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


var generateGamePromise = function(users, time, mapToFind, social) {
	var map;
	var p = mapManager.findMapByKey(mapToFind);
	return p.then(function(mapFromKey) {
		map = mapFromKey;
		return rankManager.findAllRanks();
	}).then(function(ranks){
		var widthToUse = Math.floor(Math.random() * (map.width.max - map.width.min + 1)) + map.width.min;
		var heightToUse = Math.ceil(widthToUse * 1.33);

		var numPlanets = Math.floor((widthToUse * heightToUse) * .28);
		numPlanets = Math.max(12, numPlanets);

		var gameTypeIndex = Math.floor(Math.random() * (map.gameType.length));
		var rankOfInitialUser = rankManager.findRankForAnXp(ranks, users[0].xp);

		var gameAttributes = {
			players : users,
			width : widthToUse,
			height : heightToUse,
			numberOfPlanets : numPlanets,
			createdTime : time,
			rankOfInitialPlayer : rankOfInitialUser.level,
			map : map.key,
			gameType : map.gameType[gameTypeIndex],
			social : social
		};
		
		

		return gameManager.createGame(gameAttributes).then(function(game) {
			return game.withPromise(game.save);
		}).then(function(game) {
			var currentUser = users[0];
			currentUser.coins--;
						
			var p = currentUser.withPromise(currentUser.save);
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

exports.inviteUserToGame = function(req, res){
	var requesterHandle = req.body.requesterHandle;
	var inviteeHandle = req.body.inviteeHandle;
	var mapKey = req.body.mapKey;
	var session = req.body.session;
		
	if(!validate({session : session, handle : requesterHandle, handle : inviteeHandle, mapKey : mapKey}, res)) {
		return;
	}
	var requestingUser;
	var inviteeUser;
	var currentGame;
		
	var p = validateSession(session, {"handle" : requesterHandle});
	p.then(function(){
		return userManager.findUserByHandle(requesterHandle);
	}).then(function(user){
		requestingUser = user;
		return mapManager.findMapByKey(mapKey);
	}).then(function(map){
		if(requestingUser.xp < map.availableFromXp){
			throw new Error("User does not have enough xp to play this map.");
		}else if(!inviteValidation.validate(requestingUser, inviteeUser)){
			throw new Error("Invalid Invite Request");
		}else{
			
			return userManager.findUserByHandle(inviteeHandle);
		}
	}).then(function(inviteUser){
		if(!inviteUser){
			throw new Error("Invalid Invite Request. Invitee does not exist.");
		}else{
			inviteeUser = inviteUser;
			return generateGamePromise([requestingUser], Date.now(), mapKey, inviteeHandle);
		}
	}).then(function(generatedGame){
		currentGame = generatedGame;
		return userManager.updateFriend(requestingUser, inviteeUser);
	}).then(function(savedUser){
		res.json(currentGame);
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.findPendingInvites = function(req, res){
	var handle = req.query['handle']; 
	var session = req.query['session'];
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var p = gameManager.findByInvitee(handle);
	p.then(function(queue){
		var returnList = _.map(queue, function(item){
			return {
				requester : minifyUser(item.players[0]),
				inviteeHandle : item.social.invitee,
				minifiedGame : {
					id : item._id,
					createdDate : item.createdDate,
					moveAvailable : true,
					winner : item.endGame.winnerHandle,
					date : item.endGame.date,
					map : item.map,
					social : item.social,
					claimavailable : false
				}
			};
		});
		res.json({items : returnList});
	}).then(null, logErrorAndSetResponse(req, res));
};

exports.findFriends = function(req, res){
	var handle = req.query['handle'];
	var session = req.query['session'];
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function(){
		return userManager.findUserByHandle(handle);
	}).then(function(user){
		var sortedFriends = _.sortBy(user.friends, function(friend){
			return -friend.played;
		});
		var result = _.map(sortedFriends, function(friend){
			return minifyUser(friend.user)
		})
		res.json({items : result});
	}).then(null, logErrorAndSetResponse(req, res));
}

var minifyUser = function(user){
	return {
		auth : user.auth,
		handle : user.handle,
		xp : user.xp
	};
}

exports.findMatchingFriends = function(req, res){
	var authIDs = req.body.authIds;
	var session = req.body.session;
	var authProvider = req.body.authProvider;
	var handle = req.body.handle;
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function(){
		var search = {};
		var searchKey = "auth." + authProvider;
		search[searchKey] = {$in : authIDs};
		return userManager.UserModel.find(search).exec();
	}).then(function(users){
		var result = _.map(users, minifyUser);
		res.json({items : result});
	}).then(null, logErrorAndSetResponse(req, res));
	
}

exports.addProviderToUser = function(req, res){
	var session = req.body.session;
	var authProvider = req.body.authProvider;
	var id = req.body.id;
	var handle = req.body.handle;
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function(){
		var searchObj = {};
		var searchKey = "auth." + authProvider;
		searchObj[searchKey] = id;
		return userManager.UserModel.findOne(searchObj).exec();
	}).then(function(user){
		if(user && user.handle !== handle){
			throw new Error("A user already exists with this " + authProvider + " linked.");
		}
		var setObj = {};
		var setKey = "auth." + authProvider;
		setObj[setKey] = id;
		return userManager.UserModel.findOneAndUpdate({handle : handle}, {$set : setObj}).exec();
	}).then(function(user){
		res.json(user);
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.cancelGame = function(req, res){
	var session = req.body.session;
	var handle = req.body.handle;
	var gameId = req.body.gameId;
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function(){
		return gameManager.GameModel.findOneAndUpdate({ $and : [{_id : gameId}, { $where : "this.players.length == 1"}]}, {$set : {state : 'C'}}).exec();
	}).then(function(game){
		if(game === null){
			throw new ErrorWithResponse('Another user has joined this game.', { success : false });
		}else{
			return userManager.addCoins(1, handle);
		}
	}).then(function(){
		return gameManager.GameModel.remove({ $and : [{_id : gameId}, {state :'C'}]}).exec();
	}).then(function(){
		res.json({success : true});
	}).then(null, logErrorAndSetResponse(req, res));
}

exports.claimGame = function(req, res){
	var session = req.body.session;
	var handle = req.body.handle;
	var gameId = req.body.gameId;
	
	if(!validate({session : session, handle : handle}, res)) {
		return;
	}
	
	var currentUser;
	
	var p = validateSession(session, {"handle" : handle});
	p.then(function(){
		return userManager.findUserByHandle(handle);
	}).then(function(user){
		currentUser = user;
		return claimValidation.validate(gameId, handle);
	}).then(function(result){
		if(!result.success){
			throw new Error('Invalid claim.');
		}
		return gameManager.findById(gameId);
	}).then(function(game){
		var losers = _.filter(game.players, function(player){
			return player.handle !== handle;
		});
		return gameManager.claimVictory(gameId, game.moveTime, currentUser, losers[0].handle);
	}).then(function(game){
		if(game === null){
			throw new Error('Unable to claim victory.');
		}
		return updateWinnersAndLosers(game);
	}).then(function(gameToReturn){
		res.json(processGameReturn(gameToReturn, handle));
	}).then(null, logErrorAndSetResponse(req, res));
	
}

var gameBuilder = require('../modules/gameBuilder'), 
	gameManager = require('../modules/model/game'), 
	userManager = require('../modules/model/user'), 
	rankManager = require('../modules/model/rank'),
	mapManager = require('../modules/model/map');

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
	gameManager.findById(searchId, function(game) {
		res.json(processGameReturn(game, playerHandle));
	});
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
			gameManager.findCollectionOfGames(user.currentGames,
					function(games) {
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
				coins : 1
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
	
	var updatedHandle = handle.replace(/^\s+/,'');
	updatedHandle = updatedHandle.replace(/\s+$/,'');
	updatedHandle = updatedHandle.replace(/[^a-z0-9_]/i);
	
	if (handle.length < 3 || handle.length > 16 || updatedHandle !== handle) {
		res.json({
			created : false,
			reason : "Invalid username"
		});
	} else {
		userManager.findUserByHandle(handle, function(user) {
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
		});
	}
}

exports.findCurrentGamesByPlayerHandle = function(req, res) {
	var playerHandle = req.query['playerHandle'];
	userManager.findUserByHandle(playerHandle, function(user) {
		if (!user) {
			res.json({});
		} else {
			gameManager.findCollectionOfGames(user.currentGames,
					function(games) {
						var returnObj = {};
						returnObj.items = games;
						res.json(returnObj);
					});
		}
	});
}

exports.performMoves = function(req, res) {
	var gameId = req.body.id;
	var moves = req.body.moves;
	var playerHandle = req.body.playerHandle;

	gameManager.performMoves(gameId, moves, playerHandle, 0, function(savedGame) {
		if(!savedGame) {
			res.json({
				error: "Could not perform move, please try again"
			});
		} else {
			if (savedGame.endGameInformation.winnerHandle) {
				userManager.findUserByHandle(savedGame.endGameInformation.winnerHandle, function(user) {
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

processGameReturn = function(game, playerWhoCalledTheUrl){

	for(var i = 0 ; i < game.moves.length; i++){
		var move = game.moves[i];
		
		if((move.playerHandle == playerWhoCalledTheUrl) && move.startingRound == game.currentRound.roundNumber){
			decrementPlanetShipNumber(game,move);
		}
	}
	
	return game;
}

decrementPlanetShipNumber = function(game, move){
	for(var i = 0; i < game.planets.length; i++){
		var planet = game.planets[i];
		if(planet.name == move.fromPlanet){
			planet.numberOfShips = planet.numberOfShips - move.fleet;
		}
	}
}

exports.addPlanetsToGame = function(req, res) {
	var gameId = req.body.id;
	var planetsToAdd = req.body.planets;
	gameManager.addPlanetsToGame(gameId, planetsToAdd, function(updatedGame) {
		res.json(updatedGame);
	});
};

exports.deleteGame = function(req, res) {
	var gameId = req.query['id'];
	gameManager.deleteGame(gameId, function() {
		res.send("Game: " + gameId + " has been Deleted");
	});
};

exports.joinGame = function(req, res) {
	var gameId = req.query['id'];
	var playerHandle = req.query['playerHandle'];

	userManager.findUserByHandle(playerHandle, function(user) {
		gameManager.addUser(gameId, user, function(game) {
			gameManager.findById(gameId, function(returnGame) {
				user.currentGames.push(gameId);
				user.save(function() {
					res.json(returnGame);
				});
			});
		});
	});
}

exports.addCoins = function(req, res){
	var playerHandle = req.body.playerHandle;
	var numCoins = req.body.numCoins;
	
	userManager.findUserByHandle(playerHandle, function(user) {
		user.coins += numCoins;
		user.usedCoins = 0;
		user.save(function() {
			res.json(user);
		});
	});
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

exports.matchPlayerToGame = function(req, res){
	var mapToFind = req.body.mapToFind;
	var time = req.body.time - 300000;
	var playerHandle = req.body.playerHandle;
	
	userManager.findUserByHandle(playerHandle, function(user){
		gameManager.findGameForMapInTimeLimit(mapToFind, time, playerHandle,  function(games){
			if(games.length > 0){
				joinAGame(games, playerHandle, time, function(game){
							if(game === null){
								generateGame(user, req.body.time, mapToFind, res);
							}else{
								res.json(game);
							}
						});
			}else{
				gameManager.findGameAtAMap(mapToFind,playerHandle, function(games){
					if(games.length > 0){
						joinAGame(games, user, time, function(game){
							if(game === null){
								generateGame(playerHandle, req.body.time, mapToFind, res);
							}else{
								res.json(game);
							}
						});
					}else{
						generateGame(playerHandle, req.body.time, mapToFind, res);
					}
				});
			}
		});
	});
}

var joinAGame = function(games, user, time, callback){

	
	var segments = {};
	
	segments["eq"] = addSegment(games, function(level){ return level == user.rankInfo.level});
	segments["lt"] = addSegment(games, function(level){ return level < user.rankInfo.level});
	segments["gt"] = addSegment(games, function(level){ return level > user.rankInfo.level});
	
	segments.eq.concat(segments.lt, segments.gt);
	
	
	addGameFromSegment(segments.eq, 0, user, time, callback);
	
}

var addGameFromSegment = function(games, index, user, time, callback){

		if(index > games.lengh){
			callback(null);
		}
	
		var gameId = games[index]._id;
		
		gameManager.addUser(gameId, user, function(game) {
			if(game !== null){
				gameManager.findById(gameId, function(returnGame) {
					userManager.findUserByHandle(user.handle, function(user){
						user.currentGames.push(gameId);
						user.coins--;
						if(user.coins == 0){
							user.usedCoins = time;
						}
						user.save(function() {
							foundGame = returnGame;
 							callback(returnGame);	
						});
					});
				});
			}else{
				addGameFromSegment(games, index++, user, time, callback);
			}	
		});		
}

var addSegment = function(games, comparator){
	var toAdd = [];
	
	for(var i = 0; i < games.length; i++){
		var game = games[i];
		if(comparator(game.rankOfInitialPlayer)){
			toAdd.push(game);
		}
	}
	
	return toAdd;

}

var generateGame = function(playerHandle, time, mapToFind, res) {

	userManager.findUserByHandle(playerHandle, function(user) {
	
		mapManager.findMapByKey(mapToFind, function(map){
			if(!map){
				res.json({
					error : "No Map Matching the key " + mapToFind
				});
			}else{
				var widthToUse = Math.floor(Math.random() * (map.width.max - map.width.min + 1)) + map.width.min;
				var heightToUse = Math.ceil(widthToUse * 1.33);
				
				var numPlanets = Math.floor((widthToUse * heightToUse) * .28);
				numPlanets = Math.max(12, numPlanets);
				
				var gameAttributes = {
								players : [ user ],
								width :  widthToUse,
								height :  heightToUse,
								numberOfPlanets :  numPlanets,
								createdTime : time,
								rankOfInitialPlayer : user.rankInfo.level,
								map : map.key,
								gameType : map.gameType
							};
				
				gameManager.createGame(gameAttributes, function(game) {
							gameManager.saveGame(game, function() {
								user.currentGames.push(game.id);
								user.coins--;
								if(user.coins == 0){
									user.usedCoins = time;
								}
								user.save(function() {
									res.json(game);
								});
		
							});
				});
			
			}
		});
		
	});
}

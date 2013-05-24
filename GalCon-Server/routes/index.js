var gameBuilder = require('../modules/gameBuilder'), gameManager = require('../modules/model/game'), userManager = require('../modules/model/user'), rankManager = require('../modules/model/rank');

/*
 * GET home page.
 */
exports.index = function(req, res) {
	res.render('index.html')
};

/*
 * Create a new game for a player. This will assign the player a home planet and
 * also return a gameboard object which will be stored in mongoDB
 */
exports.generateGame = function(req, res) {
	var playerHandle = req.body.playerHandle;

	userManager.findUserByHandle(playerHandle, function(user) {
		var numPlanets = Math.floor(req.body.width * req.body.height / 4);
		numPlanets = Math.max(12, numPlanets);
		gameManager.createGame([ user ], req.body.width, req.body.height, numPlanets,
				req.body.gameType, function(game) {
					gameManager.saveGame(game, function() {
						user.currentGames.push(game.id);
						user.save(function() {
							res.json(game);
						});

					});
				});
	});
}

// Find all games currently available in the system.
exports.findAllGames = function(req, res) {
	gameManager.findAllGames(function(games) {
		var returnObj = {};
		returnObj.items = games;
		res.json(returnObj);
	});
}

// Find a specific game by its object id
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
							if (games[len].currentRound.playerHandle != user.handle
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
				losses : 0
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
	userManager.findUserByHandle(handle, function(user) {
		if (user) {
			res.json({
				created : false
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

	gameManager.performMoves(gameId, moves, playerHandle, function(savedGame) {
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
		console.log(planet.name  + " : " + move.fromPlanet);
		if(planet.name == move.fromPlanet){
			planet.numberOfShips = planet.numberOfShips - move.fleet;
			console.log("Decresaing for planet " + planet.name);
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

// Join a game will use the game ID to add a player to a game.
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

exports.findRankInformation = function(req, res) {
	var rank = req.query['rank'];
	rankManager.findRankByName(rank, function(dbRank) {
		res.json(dbRank);
	});
}

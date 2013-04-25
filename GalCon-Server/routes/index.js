var gameBuilder = require('../modules/gameBuilder')
, gameManager = require('../modules/model/game')
, userManager = require('../modules/model/user')
, rankManager = require('../modules/model/rank');


/*
 * GET home page.
 */
exports.index = function(req, res){
  res.render('index.html')
};

/*
 * Create a new game for a player. This will assign the player a home planet and also return
 * a gameboard object which will be stored in mongoDB
 */
exports.generateGame = function(req, res){
	var player = req.body.player;
	gameManager.createGame([player], req.body.width, req.body.height, 10, function(game){
		gameManager.saveGame(game, function(){
			userManager.findUserByName(player, function(user){
				user.currentGames.push(game.id);
				
				user.save(function(){
					res.json(game);
				});
				
			});
		});
	});
}

// Find all games currently available in the system.
exports.findAllGames = function(req, res){
	gameManager.findAllGames(function(games){
		var returnObj = {};
		returnObj.items = games;
		res.json(returnObj);
	});
}

// Find a specific game by its object id
exports.findGameById = function(req, res){
	var searchId = req.query['id'];
	gameManager.findById(searchId, function(game){
		res.json(game);
	});
}

exports.findAvailableGames = function(req, res){
	var player = req.query['player'];
	gameManager.findAvailableGames(player, function(games){
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
						while(len--) {
							if(games[len].currentRound.player != user.name) {
								games.splice(len, 1);
							}
						}
						returnObj.items = games;
						res.json(returnObj);
					});
		}
	});
}

exports.findUserByUserName = function(req, res){
	var userName = req.query['userName'];
	userManager.findUserByName(userName, function(user){
		if(user){
			res.json(user);
		}else{
			user = new userManager.UserModel({
				name : userName,
				currentGames : [],
				xp : 0,
				rank : 1					
			});
			
			user.save(function(){
				res.json(user);
			});
		}		
	});
}

exports.findCurrentGamesByUserName = function(req, res) {
	var userName = req.query['userName'];
	userManager.findUserByName(userName, function(user) {
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

exports.performMoves = function(req, res){
	var gameId = req.body.id;
	var moves = req.body.moves;
	var player = req.body.player;
	gameManager.performMoves(gameId, moves, player, function(savedGame){
		if(savedGame.winner){
			userManager.findUserByName(savedGame.winner, function(user){
					user.xp += 10;
					rankManager.findRankForXp(user.xp, function(rank){
						user.rank = rank.name;
						user.save(function(){
							res.json(savedGame);
						});
					});
			});
		}else{
			res.json(savedGame);
		}
	});
}

exports.addPlanetsToGame = function(req, res){
	var gameId = req.body.id;
	var planetsToAdd = req.body.planets;
	gameManager.addPlanetsToGame(gameId,planetsToAdd, function(updatedGame){
		res.json(updatedGame);
	});
};

exports.deleteGame = function(req, res){
	var gameId = req.query['id'];
	gameManager.deleteGame(gameId, function(){
		res.send("Game: " + gameId + " has been Deleted");
	});
};

// JOin a game will use the game ID to add a player to a game.
exports.joinGame = function(req, res){
	var gameId = req.query['id'];
	var player = req.query['player'];
	gameManager.addUser(gameId, player,  function(game){
		gameManager.findById(gameId, function(returnGame){
			userManager.findUserByName(player, function(user){
				user.currentGames.push(gameId);
				user.save(function(){
					res.json(returnGame);
				});
				
			});
		});
	});

}

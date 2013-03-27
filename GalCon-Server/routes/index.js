var gameBuilder = require('../modules/gameBuilder')
, gameManager = require('../modules/model/game')
, userManager = require('../modules/model/user');


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
	gameManager.createGame([player], 10,function(game){
		gameManager.saveGame(game, function(){
			var user = new userManager.UserModel({
				name : player,
				currentGames : [game.id]
			});
			user.createOrAdd(game.id, function(user){
				res.json(game);
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

// Find where the number of players currently playing is 1
exports.findAvailableGames = function(req, res){
	gameManager.findAvailableGames(function(games){
		var returnObj = {};
		returnObj.items = games;
		res.json(returnObj);
	});
}

exports.findUserByUserName = function(req, res){
	var userName = req.query['userName'];
	userManager.findUserByName(userName, function(user){
		res.json(user);
	});
}

exports.performMoves = function(req, res){
	var gameId = req.body.id;
	var moves = req.body.moves;
	var player = req.body.player;
	gameManager.performMoves(gameId, moves, player, function(savedGame){
		res.json(savedGame);
	});
}

exports.addPlanetsToGame = function(req, res){
	var gameId = req.body.id;
	var planetsToAdd = req.body.planets;
	gameManager.addPlanetsToGame(gameId,planetsToAdd, function(updatedGame){
		res.json(updatedGame);
	});
};

// JOin a game will use the game ID to add a player to a game.
exports.joinGame = function(req, res){
	var gameId = req.query['id'];
	var player = req.query['player'];
	gameManager.addUser(gameId, player,  function(game){
		gameManager.findById(gameId, function(returnGame){
			var user = new userManager.UserModel({
				name : player			
			});
			user.createOrAdd(returnGame.id, function(user){
				res.json(returnGame);
			});
		});
	});

}
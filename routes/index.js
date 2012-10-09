var gameBuilder = require('../modules/gameBuilder')
, gameManager = require('../modules/model/game');


/*
 * GET home page.
 */
exports.index = function(req, res){
  res.render('index.html')
};

exports.generateGame = function(req, res){
	var player = req.body.player;
	gameManager.createGame([player], 10,function(game){
		gameManager.saveGame(game, function(){
			res.json(game);
		});
	});
}

exports.findAllGames = function(req, res){
	console.log("Searching for all games.");
	gameManager.findAllGames(function(games){
		var returnObj = {};
		returnObj.items = games;
		res.json(returnObj);
	});
}

exports.findGameById = function(req, res){
	var searchId = req.query['id'];
	console.log("Searching for game by ID: " + searchId);
	gameManager.findById(searchId, function(game){
		res.json(game);
	});
}


exports.joinGame = function(req, res){
	var gameId = req.query['id'];
	var player = req.query['player'];
	gameManager.addUser(gameId, player,  function(game){
		gameManager.findById(gameId, function(returnGame){
			res.json(returnGame);
		});
	});

}
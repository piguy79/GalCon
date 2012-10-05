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
		res.json(games);
	});
}
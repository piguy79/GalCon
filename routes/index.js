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
	/*var game = gameBuilder.createGameBuilder([player], 10);
	game.createBoard(function(createdGame){
		gameDao.saveGame(createdGame, function(){
			res.json(createdGame);
		});
		
	});*/
}
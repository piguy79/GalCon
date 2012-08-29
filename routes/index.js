
/*
 * GET home page.
 */
exports.index = function(req, res){
  res.render('index.html')
};

exports.generateGame = function(req, res){
	var player = req.body.player;
	var game = {};
	game['players'] = [player];
	game['gameId'] = 12;


	res.json(game);
}
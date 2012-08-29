
/*
 * GET home page.
 */

exports.index = function(req, res){
  res.render('index.html')
};

exports.generateGame = function(req, res){
	console.log(req.body.player);
	var game = {};
	game['players'] = ["Matt","Conor"];
	game['gameId'] = 12;


	res.json(game);
}
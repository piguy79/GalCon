var galconMath = require("../math/galconMath");

exports.adjustMovePositions = function(game) {
	for(var i = 0; i < game.moves.length; i++){
		var move = game.moves[i];
		
		move.prevPos = move.curPos;
		
		var startPos;
		var endPos;
		for(i in game.planets) {
			if(game.planets[i].name === move.from) {
				startPos = game.planets[i].pos;
			}
			if(game.planets[i].name === move.to) {
				endPos = game.planets[i].pos;
			}
		}
		
		var totalDuration = galconMath.distance(startPos, endPos);
		
		var percentTraveled = 1 - (move.duration / totalDuration);
		
		var currentX = startPos.x + (endPos.x - startPos.x) * percentTraveled;
		var currentY = startPos.y + (endPos.y - startPos.y) * percentTraveled;
		
		move.curPos = {x : currentX, y : currentY};
	}
}

var galconMath = require("../math/galconMath");

exports.adjustMovePositions = function(game) {
	for(var i = 0; i < game.moves.length; i++){
		var move = game.moves[i];
		
		move.prevPos = move.curPos;
		
		var startPos;
		var endPos;
		for(j in game.planets) {
			if(game.planets[j].name == move.from) {
				startPos = game.planets[j].pos;
			}
			if(game.planets[j].name == move.to) {
				endPos = game.planets[j].pos;
			}
		}
		
		var totalDuration = galconMath.distance(startPos, endPos);
		
		var percentTraveled = 1 - (move.duration / totalDuration);
		
		var currentX = startPos.x + (endPos.x - startPos.x) * percentTraveled;
		var currentY = startPos.y + (endPos.y - startPos.y) * percentTraveled;
		
		move.curPos = {x : currentX, y : currentY};
	}
}

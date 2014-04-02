var galconMath = require("../math/galconMath");

exports.adjustMovePositions = function(game) {
	for(var i = 0; i < game.moves.length; i++){
		var move = game.moves[i];
		
		move.prevPos = move.curPos;
		
		var totalDuration = galconMath.distance(move.startPos, move.endPos);
		
		var percentTraveled = 1 - (move.duration / totalDuration);
		
		var currentX = move.startPos.x + (move.endPos.x - move.startPos.x) * percentTraveled;
		var currentY = move.startPos.y + (move.endPos.y - move.startPos.y) * percentTraveled;
		
		move.curPos = {x : currentX, y : currentY};
	}
}

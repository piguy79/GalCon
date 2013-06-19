var galconMath = require("../math/galconMath");

exports.adjustMovePositions = function(game) {

	for(var i =0; i < game.moves.length; i++){

		var move = game.moves[i];
		
		move.previousPosition = move.currentPosition;
		
		var totalDuration = galconMath.distance(move.startPosition, move.endPosition);
		//var percentTraveled = 1 / (1 - (move.duration / totalDuration));
		
		// Something wrong, midpoint works fine
		
		//var currentX = (move.startPosition.x + move.endPosition.x) / 2;
	    //var currentY = (move.startPosition.y + move.endPosition.y) / 2;
		
		var percentTraveled = 1 - (move.duration / totalDuration);
		
		var currentX = move.startPosition.x + (move.endPosition.x - move.startPosition.x) * percentTraveled;
		var currentY = move.startPosition.y + (move.endPosition.y - move.startPosition.y) * percentTraveled;
		
		move.currentPosition = {x : currentX, y : currentY};

	}

}

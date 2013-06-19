

exports.distance = function(startPosition, endPosition){
	return Math.sqrt(Math.pow(endPosition.x - startPosition.x, 2.0) + Math.pow(endPosition.y - startPosition.y, 2.0));
}
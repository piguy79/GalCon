exports.createPlanetForTest = function(name, owner, shipRegenRate, numberOfShips, position) {
	return {
		name : name,
		owner : owner,
		shipRegenRate : shipRegenRate,
		numberOfShips : numberOfShips,
		position : position
	};
}

exports.createMoveForTest = function(player, fromPlanet, toPlanet, fleet, duration) {
	return {
		player : player,
		fromPlanet : fromPlanet,
		toPlanet : toPlanet,
		fleet : fleet,
		duration : duration
	}
}
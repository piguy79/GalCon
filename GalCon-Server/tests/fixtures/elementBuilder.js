exports.createPlanet = function(name, owner, shipRegenRate, numberOfShips, position) {
	return {
		name : name,
		ownerHandle : owner,
		shipRegenRate : shipRegenRate,
		numberOfShips : numberOfShips,
		position : position
	};
}

exports.createMove = function(playerHandle, fromPlanet, toPlanet, fleet, duration) {
	return {
		playerHandle : playerHandle,
		fromPlanet : fromPlanet,
		toPlanet : toPlanet,
		fleet : fleet,
		duration : duration
	};
}

exports.createUser = function(playerHandle, level) {
	return {
		name: playerHandle + "@gmail.com",
		handle: playerHandle,
		rankInfo: {
			level : level,
			startFrom : 0,
			endAt : 50
		},
		xp : 0,
		wins : 0,
		losses : 0,
		coins : 0,
		usedCoins : -1,
		watchedAd : false
	};
}

exports.createMap = function(key, widthMin, widthMax) {
	return {
		"key" : key,
		"availableFromLevel" : 1,
		"title" : "TEST_MAP: " + key,
		"description" : "Test map " + key,
		"width" : {
			"min" : widthMin,
			"max" : widthMax
		},
		"gameType" : ["standardGame"]
	};
}
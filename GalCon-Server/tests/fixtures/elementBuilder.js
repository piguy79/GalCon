exports.createPlanet = function(name, owner, shipRegenRate, numberOfShips, position, ability) {
	return {
		name : name,
		ownerHandle : owner,
		shipRegenRate : shipRegenRate,
		numberOfShips : numberOfShips,
		position : position,
		ability : ability || '',
		status : 'ALIVE'
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

exports.createUser = function(playerHandle, level, config) {
	if(!config){
		config = {};
	}
	return {
		name: playerHandle + "@gmail.com",
		handle: playerHandle,
		rankInfo: {
			level : level,
			startFrom : 0,
			endAt : 50
		},
		xp : config.xp || 0,
		wins : config.wins || 0,
		losses : config.losses || 0,
		coins : config.coins || 1,
		usedCoins : config.usedCoins || -1,
		watchedAd : config.watchedAd || false
	};
}

exports.createMap = function(key, widthMin, widthMax, gameType) {
	return {
		"key" : key,
		"availableFromLevel" : 1,
		"title" : "TEST_MAP: " + key,
		"description" : "Test map " + key,
		"width" : {
			"min" : widthMin,
			"max" : widthMax
		},
		"gameType" : gameType || ["standardGame"]
	};
}
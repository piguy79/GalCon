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
		email: playerHandle + "@gmail.com",
		handle: playerHandle,
		rankInfo: {
			level : level,
			startFrom : 0,
			endAt : 50
		},
		session :  {
			id : "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592",
			expireDate : Date.now() + 4 * 60 * 60 * 1000
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
exports.createPlanet = function(name, owner, regen, ships, position, ability) {
	return {
		name : name,
		handle : owner,
		regen : regen,
		ships : ships,
		pos : position,
		ability : ability || '',
		status : 'ALIVE'
	};
}

exports.createMove = function(handle, from, to, fleet, duration) {
	return {
		handle : handle,
		from : from,
		to : to,
		fleet : fleet,
		duration : duration
	};
}

exports.createUser = function(playerHandle, level, config) {
	if(!config){
		config = {};
	}
	var expireDate = Date.now() + 4 * 60 * 60 * 1000;
	
	return {
		auth: {
			google : playerHandle + "@gmail.com",
			facebook : playerHandle + "@facebook.com"
		},
		handle: playerHandle,
		rankInfo: {
			level : level,
			startFrom : 0,
			endAt : 100
		},
		session :  {
			id : "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592",
			expireDate : config.expireDate || expireDate
		},
		xp : config.xp || 5500,
		wins : config.wins || 0,
		losses : config.losses || 0,
		coins : config.coins || 5
	};
}

exports.createMap = function(key, widthMin, widthMax, gameType, availableFromXp) {
	return {
		"key" : key,
		"availableFromXp" : availableFromXp || 1,
		"title" : "TEST_MAP: " + key,
		"description" : "Test map " + key,
		"width" : {
			"min" : widthMin,
			"max" : widthMax
		},
		"gameType" : gameType || ["standardGame"]
	};
}

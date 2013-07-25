gameTypeAssembler = require('./model/gameType/gameTypeAssembler');


var MAX_REGEN = 5;
var MAX_STARTING_SHIPS = 10;
var HOME_RADIUS_RATIO = .38;

function GameBuilder(players, width, height, numberOfPlanets, gameType) {
	this.currentPlanetNum = 0;
	this.version = 0;
	this.players = players;
	this.width = width;
	this.height = height;
	this.createdDate = new Date();
	this.currentRound = {
		roundNumber : 0,
		playersWhoMoved : []
	};
	this.endGameInformation = {
		winnerHandle : "",
		losers : [],
		draw : false,
		xpAwardToWinner : 0
	}
	this.ability = "";
	this.numberOfPlanets = numberOfPlanets;
	this.gameType = gameType;
	this.planets = [];
	
	if(gameTypeAssembler.gameTypes[gameType].constructGameBoard){
			gameTypeAssembler.gameTypes[gameType].constructGameBoard(this,players, width, height, numberOfPlanets);
	}
}
GameBuilder.prototype.constructor = GameBuilder;
GameBuilder.prototype.players = [];

GameBuilder.prototype.createBoard = function(callback) {
	this.createHomePlanets();
	
	var boardSize = this.width * this.height;
	var regenAroundHomePlanet = Math.floor(boardSize * .21); 
	var shipsAroundHomePlanet = Math.floor(boardSize * .23);
	this.createPlanetsAroundHomePlanet(this.planets[0], regenAroundHomePlanet, shipsAroundHomePlanet);
	this.createPlanetsAroundHomePlanet(this.planets[1], regenAroundHomePlanet, shipsAroundHomePlanet, this.planets[0]);
	
	this.createRemainingPlanets([this.planets[0], this.planets[1]]);

	assignHomePlanets(this);
	callback(this);
}

GameBuilder.prototype.createRemainingPlanets = function(homePlanets) {
	var tooCloseToHomeRadius = Math.floor(this.width * HOME_RADIUS_RATIO);
	
	var extraPlanets = [];
	while(this.planets.length < this.numberOfPlanets) {
		var newPosition = this.createRandomPosition();
		var noGood = false;
		for(i in homePlanets) {
			if(this.distanceBetweenPositions(homePlanets[i].position, newPosition) <= tooCloseToHomeRadius) {
				noGood = true;
			}
		}
		
		if(!noGood) {
			for(i in this.planets) {
				if(newPosition.x == this.planets[i].position.x && newPosition.y == this.planets[i].position.y) {
					noGood = true;
				}
			}
		}
		
		if(!noGood) {
			var planet = this.createPlanet(newPosition.x, newPosition.y);
			planet.shipRegenRate = Math.floor((Math.random() * MAX_REGEN) + 1);
			planet.numberOfShips = Math.floor(Math.random() * MAX_STARTING_SHIPS);
			this.planets.push(planet);
			extraPlanets.push(planet);
		}
	}
	
	if(gameTypeAssembler.gameTypes[this.gameType].addPlanetAbilities){
		gameTypeAssembler.gameTypes[this.gameType].addPlanetAbilities(extraPlanets);
	}
}

GameBuilder.prototype.createHomePlanets = function() {
	var homePlanets = [];
	var minDistanceBetweenPlanets = ((this.width - 2) * HOME_RADIUS_RATIO) + 1;
	
	var xMidPoint = (this.width - 1) / 2.0;
	var yMidPoint = (this.height - 1) / 2.0;
	var x = xMidPoint;
	var y = yMidPoint;
	while(x == xMidPoint && y == yMidPoint) {
		x = Math.floor(Math.random() * this.width);
		y = Math.floor(Math.random() * this.height);
		
		x = this.removeFromEdge(x, this.width-1);
		y = this.removeFromEdge(y, this.height-1);
	}
	
	homePlanets.push(this.createHomePlanet(x, y));

	while (homePlanets.length != 2) {
		var x = Math.floor((Math.random() * this.width));
		var y = Math.floor((Math.random() * this.height));
		
		x = this.removeFromEdge(x, this.width-1);
		y = this.removeFromEdge(y, this.height-1);

		var xDist = x - homePlanets[0].position.x;
		var yDist = y - homePlanets[0].position.y;

		var distance = Math.sqrt(xDist * xDist + yDist * yDist);
		if (distance <= minDistanceBetweenPlanets) {
			continue;
		}

		homePlanets.push(this.createHomePlanet(x, y));
	}

	this.planets.push(homePlanets[0]);
	this.planets.push(homePlanets[1]);
}

GameBuilder.prototype.removeFromEdge = function(value, max) {
	if(value == 0) {
		value = 1;
	} else if(value == max) {
		value--;
	}
	
	return value;
}

GameBuilder.prototype.createPlanetsAroundHomePlanet = function(planet, totalRegenAroundPlanet, shipsAroundPlanet, otherHomePlanet) {
	var acceptableRadius = Math.floor(this.width * HOME_RADIUS_RATIO);
	
	var existingRegenAroundPlanet = this.sumValueAroundPlanet(planet, acceptableRadius, "shipRegenRate");
	totalRegenAroundPlanet -= existingRegenAroundPlanet;
	
	var newPlanets = [];
	while (totalRegenAroundPlanet > 0) {
		var newPlanetRegen;
		if (totalRegenAroundPlanet <= MAX_REGEN) {
			newPlanetRegen = totalRegenAroundPlanet;
		} else {
			newPlanetRegen = Math.floor((Math.random() * MAX_REGEN) + 1);
		}

		var position = this.findNewPositionNearPlanet(planet, acceptableRadius, otherHomePlanet);
		if (position === undefined) {
			// All positions are occupied.  Increment the count on existing planets instead
			for (i in newPlanets) {
				if (totalRegenAroundPlanet > 0) {
					var oldRegen = newPlanets[i].shipRegenRate;
					var newRegen = Math.min(MAX_REGEN, oldRegen + 1);
					newPlanets[i].shipRegenRate = newRegen;
					totalRegenAroundPlanet--;
				}
			}
		} else {
			var newPlanet = this.createPlanet(position.x, position.y);
			newPlanet.shipRegenRate = newPlanetRegen;
			newPlanets.push(newPlanet);
			this.planets.push(newPlanet);

			totalRegenAroundPlanet -= newPlanetRegen;
		}
	}
	
	var existingShipsAroundPlanet = this.sumValueAroundPlanet(planet, acceptableRadius, "numberOfShips");
	shipsAroundPlanet -= existingShipsAroundPlanet;
	
	for(i in newPlanets) {
		if(i == newPlanets.length - 1) {
			newPlanets[i].numberOfShips = shipsAroundPlanet;
		} else {
			var newPlanetShips = Math.floor(Math.random() * Math.min(MAX_STARTING_SHIPS, shipsAroundPlanet));
			newPlanets[i].numberOfShips = newPlanetShips;
		
			shipsAroundPlanet -= newPlanetShips;
		}
	}
}

GameBuilder.prototype.findNewPositionNearPlanet = function(planet, radius, notNearPlanet) {
	var position;
	
	var i = 0;
	while(position === undefined && i < 100) {
		var testPosition = this.createRandomPosition();
		var dist = this.distanceBetweenPositions(planet.position, testPosition);
		if(dist > 0 && dist <= radius) {
			if(notNearPlanet) {
				var awayDist = this.distanceBetweenPositions(notNearPlanet.position, testPosition);
				if(awayDist <= radius) {
					continue;
				}
			}
			
			var isGoodPosition = true;
			for(i in this.planets) {
				if(testPosition.x == this.planets[i].position.x && testPosition.y == this.planets[i].position.y) {
					isGoodPosition = false;
				}
			}
			
			if(isGoodPosition) {
				position = testPosition;
			}
		}
		++i;
	}
	
	return position;
}

GameBuilder.prototype.sumValueAroundPlanet = function(planet, radius, field) {
	var total = 0;
	for (i in this.planets) {
		if(this.planets[i].isHome == "Y") {
			continue;
		}
		
		if (this.distanceBetweenPositions(planet.position, this.planets[i].position) <= radius) {
			total += eval("this.planets[i]." + field);
		}
	}

	return total;
}

GameBuilder.prototype.distanceBetweenPositions = function(position1, position2) {
	var x = position1.x - position2.x;
	var y = position1.y - position2.y;
	return Math.sqrt(x*x + y*y);
}

GameBuilder.prototype.createHomePlanet = function(x, y) {
	var planet = this.createPlanet(x, y);
	
	planet.numberOfShips = 30;
	planet.shipRegenRate = 5;
	planet.isHome = "Y";

	return planet;
}

GameBuilder.prototype.createPlanet = function(x, y) {	
	var planet = {};
	var position = {};
	position.x = x;
	position.y = y;

	planet.name = "Planet: " + this.currentPlanetNum++;
	planet.position = position;
	planet.shipRegenRate = 0;
	planet.numberOfShips = 0;
	planet.ability = "";

	return planet;
}

GameBuilder.prototype.createRandomPosition = function() {
	var position = {};
	
	position.x = Math.floor(Math.random() * this.width);
	position.y = Math.floor(Math.random() * this.height);
	
	return position;
}

function assignHomePlanets(builder) {
	builder.players.forEach(function(player) {
		for ( var i in builder.planets) {
			var planet = builder.planets[i];
			if (!planet.ownerHandle && planet.isHome == "Y") {
				planet.ownerHandle = player.handle;
				break;
			}
		}
	});
}

exports.createGameBuilder = function(players, width, height, numberOfPlanets, gameType) {
	return new GameBuilder(players, width, height, numberOfPlanets, gameType);
}

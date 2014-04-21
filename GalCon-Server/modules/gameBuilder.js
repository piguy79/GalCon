var gameTypeAssembler = require('./model/gameType/gameTypeAssembler'),
	galconMath = require('./math/galconMath');

var MAX_REGEN = 5;
var MAX_STARTING_SHIPS = 10;
var HOME_RADIUS_RATIO = .38;
var MAX_POPULATION = 50000000;

function GameBuilder(gameAttributes) {
	this.currentPlanetNum = 0;
	this.version = 0;
	this.players = gameAttributes.players;
	this.width = gameAttributes.width;
	this.height = gameAttributes.height;
	this.createdDate = Date.now();
	this.moveTime = this.createdDate;
	this.rankOfInitialPlayer = gameAttributes.rankOfInitialPlayer;
	this.map = gameAttributes.map;
	
	if(gameAttributes.social){
		this.social = {
			invitee : gameAttributes.social,
			status : "CREATED"
		};
	}
	
	this.round = {
		num : 0,
		moved : []
	};
	this.endGame = {
		winnerHandle : "",
		losers : [],
		draw : false,
		xp : 0
	}
	this.ability = "";
	this.numberOfPlanets = gameAttributes.numberOfPlanets;
	this.gameType = gameAttributes.gameType;
	this.planets = [];
	
	if(gameTypeAssembler.gameTypes[gameAttributes.gameType].constructGameBoard){
			gameTypeAssembler.gameTypes[gameAttributes.gameType].constructGameBoard(this,gameAttributes.players, gameAttributes.width, gameAttributes.height, gameAttributes.numberOfPlanets);
	}
}
GameBuilder.prototype.constructor = GameBuilder;
GameBuilder.prototype.players = [];

GameBuilder.prototype.createBoard = function() {
	this.createHomePlanets();
	
	var boardSize = this.width * this.height;
	var regenAroundHomePlanet = Math.floor(boardSize * .21); 
	var shipsAroundHomePlanet = Math.floor(boardSize * .23);
	this.createPlanetsAroundHomePlanet(this.planets[0], regenAroundHomePlanet, shipsAroundHomePlanet);
	this.createPlanetsAroundHomePlanet(this.planets[1], regenAroundHomePlanet, shipsAroundHomePlanet, this.planets[0]);
	
	this.createRemainingPlanets([this.planets[0], this.planets[1]]);

	assignHomePlanets(this);
}

GameBuilder.prototype.radiusAroundHome = function() {
	var acceptableRadius = Math.floor(this.width * HOME_RADIUS_RATIO);
	return Math.max(acceptableRadius, 2);
}

GameBuilder.prototype.createRemainingPlanets = function(homePlanets) {
	var tooCloseToHomeRadius = this.radiusAroundHome();
	
	var extraPlanets = [];
	while(this.planets.length < this.numberOfPlanets) {
		var newPosition = this.createRandomPosition();
		var noGood = false;
		for(i in homePlanets) {
			if(this.distanceBetweenPositions(homePlanets[i].pos, newPosition) <= tooCloseToHomeRadius) {
				noGood = true;
			}
		}
		
		if(!noGood) {
			for(i in this.planets) {
				if(newPosition.x == this.planets[i].pos.x && newPosition.y == this.planets[i].pos.y) {
					noGood = true;
				}
			}
		}
		
		if(!noGood) {
			var planet = this.createPlanet(newPosition.x, newPosition.y);
			planet.regen = Math.floor((Math.random() * MAX_REGEN) + 1);
			planet.ships = Math.floor(Math.random() * MAX_STARTING_SHIPS);
			this.planets.push(planet);
			extraPlanets.push(planet);
		}
	}
	
	if(gameTypeAssembler.gameTypes[this.gameType].addPlanetAbilities){
		var abilitiesToAdd = extraPlanets.length * 0.2;
		if (abilitiesToAdd < 1) {
			abilitiesToAdd = 1;
		}
		
		/*
		 * Use random generation to attempt balance out locations of ability planets.
		 */
		var attempt = 0,
			minDiff = 10000,
			minDiffPlanets = [];
		while(attempt < 3) {
			var usedPlanets = [];
			for(var i = 0; i < abilitiesToAdd; ++i) {
				var index = Math.ceil((extraPlanets.length * Math.random())) - 1;
				while(_.indexOf(usedPlanets, extraPlanets[index]) > -1) {
					index = Math.ceil((extraPlanets.length * Math.random())) - 1;
				}
				
				usedPlanets.push(extraPlanets[index]);
			}
			
			var avgHome1Distance = 0.0;
			var avgHome2Distance = 0.0;
			for(var i = 0; i < usedPlanets.length; ++i) {
				avgHome1Distance += galconMath.distance(homePlanets[0].pos, usedPlanets[i].pos);
				avgHome2Distance += galconMath.distance(homePlanets[1].pos, usedPlanets[i].pos);
			}
			
			var diff = Math.abs((avgHome1Distance / usedPlanets.length) - (avgHome2Distance/ usedPlanets.length));
			if(diff < minDiff) {
				minDiff = diff;
				minDiffPlanets = usedPlanets;
			}

			attempt++;
		}
		
		gameTypeAssembler.gameTypes[this.gameType].addPlanetAbilities(minDiffPlanets);
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

		var xDist = x - homePlanets[0].pos.x;
		var yDist = y - homePlanets[0].pos.y;

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
	var acceptableRadius = this.radiusAroundHome();
	
	var existingRegenAroundPlanet = this.sumValueAroundPlanet(planet, acceptableRadius, "regen");
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
					var oldRegen = newPlanets[i].regen;
					var newRegen = Math.min(MAX_REGEN, oldRegen + 1);
					newPlanets[i].regen = newRegen;
					totalRegenAroundPlanet--;
				}
			}
		} else {
			var newPlanet = this.createPlanet(position.x, position.y);
			newPlanet.regen = newPlanetRegen;
			newPlanets.push(newPlanet);
			this.planets.push(newPlanet);

			totalRegenAroundPlanet -= newPlanetRegen;
		}
	}
	
	var existingShipsAroundPlanet = this.sumValueAroundPlanet(planet, acceptableRadius, "ships");
	shipsAroundPlanet -= existingShipsAroundPlanet;
	
	for(i in newPlanets) {
		if(i == newPlanets.length - 1) {
			newPlanets[i].ships = shipsAroundPlanet;
		} else {
			var newPlanetShips = Math.floor(Math.random() * Math.min(MAX_STARTING_SHIPS, shipsAroundPlanet));
			newPlanets[i].ships = newPlanetShips;
		
			shipsAroundPlanet -= newPlanetShips;
		}
	}
}

GameBuilder.prototype.findNewPositionNearPlanet = function(planet, radius, notNearPlanet) {
	var position;
	
	var i = 0;
	while(position === undefined && i < 100) {
		var testPosition = this.createRandomPosition();
		var dist = this.distanceBetweenPositions(planet.pos, testPosition);
		if(dist > 0 && dist <= radius) {
			if(notNearPlanet) {
				var awayDist = this.distanceBetweenPositions(notNearPlanet.pos, testPosition);
				if(awayDist <= radius) {
					continue;
				}
			}
			
			var isGoodPosition = true;
			for(i in this.planets) {
				if(testPosition.x == this.planets[i].pos.x && testPosition.y == this.planets[i].pos.y) {
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
		
		if (this.distanceBetweenPositions(planet.pos, this.planets[i].pos) <= radius) {
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
	
	planet.ships = 30;
	planet.regen = 5;
	planet.isHome = "Y";

	return planet;
}

GameBuilder.prototype.createPlanet = function(x, y) {	
	var planet = {};
	var position = {};
	position.x = x;
	position.y = y;

	planet.name = "Planet: " + this.currentPlanetNum++;
	planet.pos = position;
	planet.regen = 0;
	planet.ships = 0;
	planet.population = Math.floor((Math.random() * MAX_POPULATION) + 1);
	planet.ability = "";
	planet.status = "ALIVE";

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
			if (!planet.handle && planet.isHome == "Y") {
				planet.handle = player.handle;
				break;
			}
		}
	});
}

exports.createGameBuilder = function(players, width, height, numberOfPlanets, gameType) {
	return new GameBuilder(players, width, height, numberOfPlanets, gameType);
}


function GameBuilder(players, width, height, numberOfPlanets){
	this.players = players;
	this.width = width;
	this.height = height;
	this.createdDate = new Date();
	this.currentRound = {roundNumber : 0, player : players[0]};
	this.numberOfPlanets = numberOfPlanets;
	this.winner = '';
	this.planets = [];
}
GameBuilder.prototype.constructor = GameBuilder;
GameBuilder.prototype.players = [];

GameBuilder.prototype.createBoard = function(callback){
	var board = [];
	var currentPlanetCount = 0;

	while(currentPlanetCount < this.numberOfPlanets){
		createPlanet(board, currentPlanetCount, this, function(board,planet,builder){
			if(planet){
				currentPlanetCount++;
				board.push(planet.position);
				builder.planets.push(planet);
			}
		});	
	}
	
	this.createHomePlanets();

	assignHomePlanets(this, function(builderWithAssignedHomePlanets){
		callback(builderWithAssignedHomePlanets);
	});
}

GameBuilder.prototype.createHomePlanets = function() {
	var homePlanets = [];
	var minDistanceBetweenPlanets = Math.floor(this.width / 2) + 1;
	while (homePlanets.length != 2) {
		var homeIndex = Math
				.floor((Math.random() * this.planets.length) + 1);
		var homePlanet = this.planets[homeIndex];
		
		if (homePlanet && !homePlanet.owner) {
			var tooClose = false;
			for(var i in homePlanets) {
				var createdPlanet = homePlanets[i];
				
				var xDist = createdPlanet.position.x - homePlanet.position.x;
				var yDist = createdPlanet.position.y - homePlanet.position.y;
				
				var distance = Math.sqrt(xDist*xDist + yDist*yDist);
				if(distance < minDistanceBetweenPlanets) {
					tooClose = true;
					break;
				}
			}
			
			if(!tooClose) {
				homePlanet.numberOfShips = 30;
				homePlanet.shipRegenRate = 5;
				homePlanet.isHome = "Y";
				homePlanets.push(homePlanet);
			}
		}
	}
}

function assignHomePlanets(builder, callback) {
	builder.players.forEach(function(player) {
		for(var i in builder.planets) {
			var planet = builder.planets[i];
			if(!planet.owner && planet.isHome == "Y") {
				planet.owner = player;
				break;
			}
		}
	});

	callback(builder);
}

function createPlanet(board, index, builder, callback){
	var x = Math.floor((Math.random()*builder.width));
	var y = Math.floor((Math.random()*builder.height));

	var planet = {};
	var position = {};
	position.x = x;
	position.y = y;
	
	for(i in board) {
		if(board[i].x == position.x && board[i].y == position.y) {
			return;
		}
	}


	planet.name = "Planet: " + index;
	planet.position = {};
	planet.position = position;
	planet.shipRegenRate = Math.floor((Math.random()*5)+1);
	planet.numberOfShips = Math.floor((Math.random()*10));
	callback(board,planet, builder);
}



exports.createGameBuilder = function(players, width, height, numberOfPlanets){
	return new GameBuilder(players, width, height, numberOfPlanets);
}

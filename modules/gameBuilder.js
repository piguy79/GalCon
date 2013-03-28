
function GameBuilder(players, numberOfPlanets){
	this.players = players;
	this.createdDate = new Date();
	this.currentRound = {roundNumber : 0, player : 0};
	this.numberOfPlanets = numberOfPlanets;
	this.planets = [];
}
GameBuilder.prototype.constructor = GameBuilder;
GameBuilder.prototype.players = [];

GameBuilder.prototype.createBoard = function(callback){
	// Create Multidimensional Array To hold the game Piece
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

	assignHomePlanets(this, function(builderWithAssignedHomePlanets){
		callback(builderWithAssignedHomePlanets);
	});
}

function assignHomePlanets(builder, callback){
	builder.players.forEach(function(player){
		var planetAssigned = false;
		while(!planetAssigned){
			var homeIndex = Math.floor((Math.random()*builder.planets.length)+1);
			var homePlanet = builder.planets[homeIndex];
			if(homePlanet && !homePlanet.owner){
				homePlanet.owner = player;
				console.log(homeIndex  + " : " + homePlanet.owner)
				console.log(builder.planets[homeIndex].owner)
				planetAssigned = true;
			}
		}
		
	});


	callback(builder);
}

function createPlanet(board, index, builder, callback){
	var x = Math.floor((Math.random()*8));
	var y = Math.floor((Math.random()*16));

	var planet = {};
	var position = {};
	position.x = x;
	position.y = y;
	board.forEach(function(pos){
		if(pos.x == position.x && pos.y == position.y){
			callback();
		}
	});


	planet.name = "Planet: " + index;
	planet.position = {};
	planet.position = position;
	planet.shipRegenRate = Math.floor((Math.random()*5)+1);
	planet.numberOfShips = Math.floor((Math.random()*20));
	callback(board,planet, builder);
}



exports.createGameBuilder = function(players, numberOfPlanets){
	return new GameBuilder(players, numberOfPlanets);
}

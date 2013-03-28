var needle = require("needle");

describe("Testing planet movement", function(){

	var game;

	beforeEach(function(done){
		needle.post("http://localhost:3000/generateGame","player=moveTest", function(err, response, body){
			game = body;
			done();
		});
		
		this.addMatchers({
			toBeOwnedBy : function(expected){
				console.log(this.actual);
				return this.actual.owner == expected;
			}
		});
	});
	
	var createTestPlanet = function(name, owner, shipRegenRate, numberOfShips, position){
		return  {
			name : name,
			owner : owner,
			shipRegenRate : shipRegenRate,
			numberOfShips : numberOfShips,
			position : position
		};
	}
	
	var createMoveForTest = function(player, fromPlanet, toPlanet, fleet, duration){
		return {
			player : player,
			fromPlanet : fromPlanet,
			toPlanet : toPlanet,
			fleet : fleet,
			duration : duration
		}
	}
	
	var performMove = function(gameId, moves, player, callBack){

		var postData = {
			moves : moves,
			id : gameId,
			player : player
		}
	
		needle.post("http://localhost:3000/performMoves",postData , function(err, response, body){
				callBack();
		});
	}
	
	var addPlanets = function(gameId, planetsForTest, callback){
		var postData = {
			id : gameId,
			planets : planetsForTest
		};
		
		needle.post("http://localhost:3000/addPlanetsToGame",postData, function(err, response, body){
			callback();
		});
		
	}
	
	var findGame = function(gameId, callback){
		needle.get("http://localhost:3000/findGameById?id=" + gameId, function(err, response, body){
			callback(body);
		});
		
	}
	
	it("Planet should be owned by user after move.", function(done){
		var planetsForTest = [
			createTestPlanet("FromPlanet", "", 3,10,{x : 3, y : 4}),
			createTestPlanet("toPlanet", "", 3, 2, {x : 3, y : 5})
		];
		var moveHolder = [
			createMoveForTest("moveTest", "fromPlanet", "toPlanet",6, 1)
		];	
		
		addPlanets(game._id, planetsForTest, function(){
			performMove(game._id, moveHolder, "moveTest", function(){
				findGame(game._id, function(game){
					expect(game.moves.length == 1).toBe(true);
					performMove(game._id, [], "otherPlayer", function(){
						findGame(game._id, function(game){
							game.planets.forEach(function(planet){
								if(planet.name == "toPlanet"){
									expect(planet).toBeOwnedBy("moveTest");
								}
							});
							done();
						});
					});
					
				});
			});
		});
	});

});
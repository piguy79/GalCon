var needle = require("needle"),
Step = require('step'),
apiRunner = require('../fixtures/apiRunner'),
elementBuilder = require('../fixtures/elementbuilder'),
elementMatcher = require('../fixtures/elementMatcher');

describe("Testing ship movement", function(){

	var game;
	
	var defaultPlanetsForTest = [
			elementBuilder.createPlanetForTest("fromPlanet", "", 3,10,{x : 3, y : 4}),
			elementBuilder.createPlanetForTest("toPlanet", "", 3, 20, {x : 3, y : 5})
	];

	beforeEach(function(done){
		apiRunner.generateGame(function(generatedGame){
			game = generatedGame;
			done();
		});
		
		this.addMatchers({
			toBeOwnedBy : function(expected){
				return this.actual.owner == expected;
			},
			toHaveShipNumber : function(expected){
				return this.actual.numberOfShips == expected;
			},
			toContainMove : function(expected){
				var gameMoves = this.actual;
				var returnVal = false;
				gameMoves.forEach(function(move){
					
					if(elementMatcher.moveEquals(move, expected)){
						returnVal = true;
					}

				});
				
				return returnVal;
			}
		});
	});	
	
	
	var createMovesWithValidationSteps = function(game, moveHolder, planetsForTest, validationMethod){
		Step (
			function addSomeTestPlanets(){
			
				apiRunner.addPlanets(game._id, planetsForTest, this);
			},
			function firstMove(){
				apiRunner.performMove(game._id, moveHolder, "moveTest", this);
			},
			function findGameFromDb(){
				apiRunner.findGame(game._id, this);
			},
			function validateMoveExists(dbGame){
				expect(dbGame.moves.length == 1).toBe(true);
				
				return true;
			},
			function secondMove(result){
				apiRunner.performMove(game._id, [], "otherPlayer", this);
			},
			function findGameFromDb(){
				apiRunner.findGame(game._id, this);
			},
			function validate(dbGame){
				validationMethod(dbGame);
			}
		);
	}
	
	it("Planet should be owned by user after move.", function(done){
		var moveHolder = [
			elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet",40, 1)
		];	
		
		createMovesWithValidationSteps(game, moveHolder, defaultPlanetsForTest, function(dbGame){
				dbGame.planets.forEach(function(planet){
					if(planet.name == "toPlanet"){
						expect(planet).toBeOwnedBy("moveTest");
					}
				});
				done();
		});
		
	});
	
	it("Planet should not be owned by user after move as not sending enough fleet.", function(done){
		var moveHolder = [
			elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet", 21, 1)
		];	
		
		createMovesWithValidationSteps(game, moveHolder, defaultPlanetsForTest, function(dbGame){
				dbGame.planets.forEach(function(planet){
					if(planet.name == "toPlanet"){
						expect(planet).not.toBeOwnedBy("moveTest");
						expect(planet).toHaveShipNumber(5);
					}
				});
				done();
		});
		
	});
	
	it("Planet should survive an attack from a smaller fleet", function(done){
		var moveHolder = [
			elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet",6, 1)
		];	
		
		createMovesWithValidationSteps(game, moveHolder, defaultPlanetsForTest, function(dbGame){
				dbGame.planets.forEach(function(planet){
					if(planet.name == "toPlanet"){
						expect(planet).toBeOwnedBy("");
						expect(planet).toHaveShipNumber(20);
					}
				});
				done();
		});
		
	});
	
	it("Move should be deleted after it is processed.", function(done){
		var testMove = elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet",6, 1);
		var moveHolder = [testMove];	
		
		createMovesWithValidationSteps(game, moveHolder, defaultPlanetsForTest, function(dbGame){
				expect(dbGame.moves).not.toContainMove(testMove);
				done();
		});
		
	});
	
	it("Should process the fleet vs numberOfShips logic before adding the regen", function(done){		
		var testMove = elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet",24, 1);
		var moveHolder = [testMove];	
		
		
		createMovesWithValidationSteps(game, moveHolder, defaultPlanetsForTest, function(dbGame){
				dbGame.planets.forEach(function(planet){
					if(planet.name == "toPlanet"){
						expect(planet).toBeOwnedBy("moveTest");
						expect(planet).toHaveShipNumber(4);
					}
				});
				expect(dbGame.moves).not.toContainMove(testMove);
				done();
		});
		
	});
	
	afterEach(function(done){
		apiRunner.deleteGame(game._id, function(){
			done();
		});
	});

});
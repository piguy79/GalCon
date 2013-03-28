var needle = require("needle"),
Step = require('step'),
apiRunner = require('../fixtures/apiRunner'),
elementBuilder = require('../fixtures/elementbuilder');

describe("Testing ship movement", function(){

	var game;

	beforeEach(function(done){
		apiRunner.generateGame(function(generatedGame){
			game = generatedGame;
			done();
		});
		
		this.addMatchers({
			toBeOwnedBy : function(expected){
				return this.actual.owner == expected;
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
		var planetsForTest = [
			elementBuilder.createPlanetForTest("FromPlanet", "", 3,10,{x : 3, y : 4}),
			elementBuilder.createPlanetForTest("toPlanet", "", 3, 2, {x : 3, y : 5})
		];
		var moveHolder = [
			elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet",6, 1)
		];	
		
		createMovesWithValidationSteps(game, moveHolder, planetsForTest, function(dbGame){
				dbGame.planets.forEach(function(planet){
					if(planet.name == "toPlanet"){
						expect(planet).toBeOwnedBy("moveTest");
					}
				});
				done();
		});
		
	});
	
	afterEach(function(done){
		apiRunner.deleteGame(game._id, function(){
			done();
		});
	});

});
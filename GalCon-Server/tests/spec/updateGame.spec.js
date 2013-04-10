var needle = require("needle"),
apiRunner = require('../fixtures/apiRunner'),
elementBuilder = require('../fixtures/elementbuilder'),
Step = require('step');

describe("Testing Game Updates", function(){

	var game;

	beforeEach(function(done){
		apiRunner.generateGame("updateTest",function(generatedGame){
			game = generatedGame;
			done();
		});
		
		this.addMatchers({
			toContainPlanet : function(expected){
			
				var contained = false;
				this.actual.forEach(function(planet){
					if(planet.name == expected.name){
						contained = true;
					}
				});
				return contained;;
			}
		});
	});
	
	it("Test update is a success", function(done){

		var testPlanet = elementBuilder.createPlanetForTest("test", "", 3, 4, {x: 4, y: 8});
		
		Step(
			function addATestPlanet(){
				apiRunner.addPlanets(game._id, [testPlanet], this);
			},
			function findTheUpdatedGame(){
				apiRunner.findGame(game._id, this);
			},
			function validateGameIsUpdated(updatedGame){
				expect(updatedGame.planets.length == 11).toBe(true);
				expect(updatedGame.planets).toContainPlanet(testPlanet);
				done();
			}
		
		);
	});
	
	afterEach(function(done){
		apiRunner.deleteGame(game._id, function(){
			done();
		});
	});
	

});
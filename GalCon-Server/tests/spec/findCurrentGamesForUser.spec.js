var needle = require("needle"),
Step = require('step'),
apiRunner = require('../fixtures/apiRunner'),
elementBuilder = require('../fixtures/elementbuilder'),
elementMatcher = require('../fixtures/elementMatcher');

describe("Testing Returning players current games", function(){

	var firstGame;
	var secondGame;
	var currentGameUser = "currentGames"

	beforeEach(function(done){
		Step(
			function generateFirstGame(){
				apiRunner.generateGame(currentGameUser,this);
			}, function assignGeneratedGame(generatedGame) {
				firstGame = generatedGame;
				return true;
			}, function generateSecondGame(){
				apiRunner.generateGame(currentGameUser,this);
			}, function assignGeneratedGame(generatedGame) {
				secondGame = generatedGame;
				return true;
			}, function finish(){
				done();
			}
		);
	});	
	
	it("Should bring back two games.", function(done){
		apiRunner.findCurrentGamesByUserName(currentGameUser, function(games){
			expect(games.length).toBe(2);
			done();
		});
	});
	
	
	afterEach(function(done){
		apiRunner.deleteGame(firstGame._id, function(){
			apiRunner.deleteGame(secondGame._id, function(){
				done();
			});
		});
	});

});
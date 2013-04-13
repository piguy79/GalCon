var needle = require("needle"),
Step = require('step'),
apiRunner = require('../fixtures/apiRunner'),
elementBuilder = require('../fixtures/elementbuilder'),
elementMatcher = require('../fixtures/elementMatcher');

describe("Testing available Game Method", function(){

	var firstUserGame;
	var secondUserGame;
	var firstUser = "user1";
	var secondUser = "user2";
	
	beforeEach(function(done){
		apiRunner.generateGame(firstUser, function(generatedGame){
			firstUserGame = generatedGame;	
			apiRunner.generateGame(secondUser, function(generatedGame){
				secondUserGame = generatedGame;
			
				done();
			});
		});
		
	});	
	
	
	it("Should not be able to see a game you have already joined.", function(done){
	
		apiRunner.findAvailableGamesForUser(secondUser,function(availableGames){
			expect(availableGames.length).toBe(1);
			expect(availableGames[0].players.length).toBe(1);
			expect(availableGames[0].players[0]).toBe(firstUser);
			done();
		});

	});
	
	it("After Joining a game I should not see that game when asking for available", function(done){
		Step(
			function joinFirstGameAsSecondUser(){
				apiRunner.joinGame(firstUserGame._id,secondUser, this);
			}, function findAvailableGamesForSecondUser(joinedGame){
				apiRunner.findAvailableGamesForUser(secondUser,this);
			}, function validateGameFound(gamesFound){
				expect(gamesFound.length).toBe(0);
				done();
			}
		);
	
	
	});
	
	
	afterEach(function(done){
		apiRunner.deleteGame(firstUserGame._id, function(){
			apiRunner.deleteGame(secondUserGame._id, function(){
				done();
			});
		});
	});


});

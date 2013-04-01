var needle = require("needle"),
apiRunner = require('../fixtures/apiRunner'),
Step = require('step');

describe("Testing Joining a game", function(){

	var game;

	beforeEach(function(done){
		apiRunner.generateGame(function(generatedGame){
			game = generatedGame;
			done();
		});
	});
	
	it("Test the user is present after joining", function(done){
	
		var playerToTest = "joinTest";
		
		Step (
			function joinTheGame(){
				apiRunner.joinGame(game._id,playerToTest, this);
			},
			function tryAndFindTheGameIJustJoined(result){
				apiRunner.findGame(game._id,this);
			},
			function validateResponseHasMyPlayerInThePlayerList(res){
				expect(res.players).toContain(playerToTest);
				done();
			}
		);
	});
	
});
	


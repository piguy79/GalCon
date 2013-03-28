var needle = require("needle"),
apiRunner = require('../fixtures/apiRunner'),
Step = require('step');

describe("Testing Game Deletion", function(){

	var game;

	beforeEach(function(done){
		apiRunner.generateGame(function(generatedGame){
			game = generatedGame;
			done();
		});
	});
	
	it("Test delete is a success", function(done){
		
		Step (
			function deleteGame(){
				apiRunner.deleteGame(game._id, this);
			},
			function tryAndFindTheDeletedGame(result){
				apiRunner.findGame(game._id,this);
			},
			function validateResponseWithNoGame(res){
				expect(res == null).toBe(true);
				done();
			}
		);
	});
	
});
	


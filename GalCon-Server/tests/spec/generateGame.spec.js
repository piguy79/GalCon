var needle = require('needle'),
apiRunner = require('../fixtures/apiRunner');

describe("Testing the Game generation logic", function(){

	it("Should create a new game on call", function(done){
		var postData = {
			player: "test",
			width: 8,
			height: 15
		}
		
		needle.post("http://localhost:3000/generateGame", postData, function(err, response, body){
			expect(response.statusCode).toBe(200);
			expect(body.players.length == 1).toBe(true);
			expect(body.players).toContain('test');
			apiRunner.deleteGame(body._id, function(){
				done();
			});
		});
	});
	
	it("Board size should be determined by parameters from the client", function(done){
		var postData = {
			player: "test",
			width: 20,
			height: 30
		}
		
		needle.post("http://localhost:3000/generateGame", postData, function(err, response, body){
			expect(response.statusCode).toBe(200);
			expect(body.width).toBe(20);
			expect(body.height).toBe(30);
			apiRunner.deleteGame(body._id, function(){
				done();
			});
		});
	});


});
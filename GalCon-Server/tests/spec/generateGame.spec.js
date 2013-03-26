var request = require("http"),
needle = require('needle');

describe("Testing the Game generation logic", function(){

	it("Should create a new game on call", function(done){
		needle.post("http://localhost:3000/generateGame","player=test", function(err, response, body){
			expect(response.statusCode).toBe(200);
			expect(body.players.length == 1).toBe(true);
			expect(body.players).toContain('test');
			done();
		});
	});

});
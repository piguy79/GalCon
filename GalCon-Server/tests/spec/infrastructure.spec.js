var request = require("http");

describe("Default Application behaviour", function() {

	it("Check application starts up.", function(done){
		request.get("http://localhost:3000", function(response){
			expect(response.statusCode).toBe(200);
			done();
		});
	}); 
 
 
});
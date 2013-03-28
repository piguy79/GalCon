var needle = require("needle");

describe("Testing Game Updates", function(){

	var game;

	beforeEach(function(done){
		needle.post("http://localhost:3000/generateGame","player=updateTest", function(err, response, body){
			game = body;
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
		var testPlanet = {
			name : "test", 
			owner : "", 
			shipRegenRate : 3, 
			numberOfShips : 4, 
			position : {
				x : 4, 
				y: 8
			}
		}
		game.planets.push(testPlanet);
		
		var postData = {
			id : game._id,
			planets : [testPlanet]
		};
		
		needle.post("http://localhost:3000/addPlanetsToGame",postData, function(err, response, body){
			needle.get("http://localhost:3000/findGameById?id=" + game._id, function(err, response, body){

				expect(body.planets.length == 11).toBe(true);
				expect(body.planets).toContainPlanet(testPlanet);
				done();
			});
		});
	});
	

});
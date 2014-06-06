var leaderboard = require('../../modules/model/leaderboard'),  
	mongoose = require('mongoose'), 
	game = require('../../modules/model/game'), 
	user = require('../../modules/model/user')
	rankManager = require('../../modules/model/rank');

describe("Leaderboard Tests - Calculate -", function() {
	var mapKey1 = "-100";
	
	var newPlayer = new user.UserModel();
	newPlayer.handle = "newPlayer";
	newPlayer.xp = 10;
	newPlayer.wins = 0;
	newPlayer.losses = 0;
	
	var averagePlayer1 = new user.UserModel();
	averagePlayer1.handle = "averagePlayer1";
	averagePlayer1.xp = 10;
	averagePlayer1.wins = 10;
	averagePlayer1.losses = 10;
	
	var averagePlayer2 = new user.UserModel();
	averagePlayer2.handle = "averagePlayer2";
	averagePlayer2.xp = 10;
	averagePlayer2.wins = 10;
	averagePlayer2.losses = 5;
	
	var ranks = [{level : 1, startFrom : 0, endAt : 100},
	             {level : 2, startFrom : 100, endAt : 200},
	             {level : 3, startFrom : 200, endAt : 300}]
	
	afterEach(function(done) {
		leaderboard.LeaderboardModel.remove().where("playerHandle").in([newPlayer.handle, averagePlayer1.handle, averagePlayer2.handle]).exec(function(err) {
			game.GameModel.remove({"map": mapKey1}).exec(function(err) {
				done();
			});
		});
	});
	

	it("Add one new leaderboard row", function(done) {
		var promise = new mongoose.Promise();
		
		promise.then(function () {
			return leaderboard.calculate();
		}).then(function() {
			done();
		}, function(err) {
			expect(err).toBe(null);
			done();
		});
		
		promise.complete();
	});
	
});

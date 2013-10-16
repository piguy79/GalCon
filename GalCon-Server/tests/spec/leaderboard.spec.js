var leaderboard = require('../../modules/model/leaderboard'),  
	mongoose = require('mongoose'), 
	game = require('../../modules/model/game'), 
	user = require('../../modules/model/user');

describe("Leaderboard Tests - Calculate and save:", function() {
	var mapKey1 = "-100";
	
	var newPlayer = new user.UserModel();
	newPlayer.handle = "newPlayer";
	newPlayer.rankInfo.level = 3;
	newPlayer.wins = 0;
	newPlayer.losses = 0;
	
	var averagePlayer1 = new user.UserModel();
	averagePlayer1.handle = "averagePlayer1";
	averagePlayer1.rankInfo.level = 3;
	averagePlayer1.wins = 10;
	averagePlayer1.losses = 10;
	
	var averagePlayer2 = new user.UserModel();
	averagePlayer2.handle = "averagePlayer2";
	averagePlayer2.rankInfo.level = 3;
	averagePlayer2.wins = 10;
	averagePlayer2.losses = 5;
	
	afterEach(function(done) {
		leaderboard.LeaderboardModel.remove().where("playerHandle").in([newPlayer.handle, averagePlayer1.handle, averagePlayer2.handle]).exec(function(err) {
			game.GameModel.remove({"map": mapKey1}).exec(function(err) {
				done();
			});
		});
	});
	
	it("Won first game", function(done) {
		var p = new mongoose.Promise;
		p.then(function() {
			return leaderboard.calculateAndSave(mapKey1, [averagePlayer1, newPlayer], averagePlayer1.handle);
		}).then(function() {
			return leaderboard.LeaderboardModel.findOne({"boardId" : mapKey1, "playerHandle" : averagePlayer1.handle}).exec();
		}).then(function(leaderboardRow) {
			expect(leaderboardRow.score).toBe(15);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(function() {
			done();
		});
		p.complete();
	});
	
	it("Won game and has existing game record", function(done) {
		var p = new mongoose.Promise;
		p.then(function() {
			return game.GameModel.update({"map" : mapKey1}, 
					{$set : {"endGameInformation.leaderboardScoreAmount" : 20, "endGameInformation.winnerHandle" : averagePlayer1.handle}}, {"upsert" : true}).exec();
		}).then(function() {
			return leaderboard.calculateAndSave(mapKey1, [averagePlayer1, averagePlayer2], averagePlayer1.handle);
		}).then(function() {
			return leaderboard.LeaderboardModel.findOne({"boardId" : mapKey1, "playerHandle" : averagePlayer1.handle}).exec();
		}).then(function(leaderboardRow) {
			expect(leaderboardRow.score).toBe(45);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(function() {
			done();
		});
		p.complete();
	});
	
	it("Lost game and has existing game record", function(done) {
		var p = new mongoose.Promise;
		p.then(function() {
			return game.GameModel.update({"map" : mapKey1}, 
					{$set : {"endGameInformation.leaderboardScoreAmount" : 20, "endGameInformation.winnerHandle" : averagePlayer1.handle}}, {"upsert" : true}).exec();
		}).then(function() {
			return leaderboard.calculateAndSave(mapKey1, [averagePlayer1, newPlayer], newPlayer.handle);
		}).then(function() {
			return leaderboard.LeaderboardModel.findOne({"boardId" : mapKey1, "playerHandle" : averagePlayer1.handle}).exec();
		}).then(function(leaderboardRow) {
			expect(leaderboardRow.score).toBe(30);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(function() {
			done();
		});
		p.complete();
	});
	
	it("Won game and has many existing game records", function(done) {
		var p = new mongoose.Promise;
		p.then(function() {
			return game.GameModel.update({"map" : mapKey1, "version" : 1}, 
					{$set : {"endGameInformation.leaderboardScoreAmount" : 5, "endGameInformation.winnerHandle" : averagePlayer1.handle}}, {"upsert" : true}).exec();
		}).then(function() {
			return game.GameModel.update({"map" : mapKey1, "version" : 2}, 
					{$set : {"endGameInformation.leaderboardScoreAmount" : 10, "endGameInformation.winnerHandle" : averagePlayer1.handle}}, {"upsert" : true}).exec();
		}).then(function() {
			return game.GameModel.update({"map" : mapKey1, "version" : 3}, 
					{$set : {"endGameInformation.leaderboardScoreAmount" : 12, "endGameInformation.winnerHandle" : averagePlayer1.handle}}, {"upsert" : true}).exec();
		}).then(function() {
			return leaderboard.calculateAndSave(mapKey1, [averagePlayer1, newPlayer], averagePlayer1.handle);
		}).then(function() {
			return leaderboard.LeaderboardModel.findOne({"boardId" : mapKey1, "playerHandle" : averagePlayer1.handle}).exec();
		}).then(function(leaderboardRow) {
			expect(leaderboardRow.score).toBe(55);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(function() {
			done();
		});
		p.complete();
	});
});

describe("Leaderboard Tests - Find Score for Single User:", function() {
	var boardId1 = "TEST_BOARD1";
	var playerHandle1 = "TEST_HANDLE1";

	beforeEach(function(done) {
		leaderboard.LeaderboardModel.remove().where("playerHandle").in([playerHandle1]).exec(function(err) {
			done();
		});
	});
	
	it("No row should exist for user", function(done) {
		var promise = leaderboard.findScore(boardId1, playerHandle1);
		promise.then(function (leaderboardRow) {
			expect(leaderboardRow).toBe(null);
			done();
		}).then(null, function(err) {
			expect(err).toBe(null);
			done();
		});
	});
	
	it("Find existing row for user", function(done) {
		var promise = leaderboard.updateScore(boardId1, playerHandle1, 5);
		promise.then(function () {
			return leaderboard.findScore(boardId1, playerHandle1);
		}).then(function (leaderboardRow) {
			expect(leaderboardRow.score).toBe(5);
			done();
		}).then(null, function(err) {
			expect(err).toBe(null);
			done();
		});
	});
});

describe("Leaderboard Tests - Update Score:", function() {
	var boardId1 = "TEST_BOARD1";
	var boardId2 = "TEST_BOARD2";
	var playerHandle1 = "TEST_HANDLE1";
	var playerHandle2 = "TEST_HANDLE2";

	beforeEach(function(done) {
		leaderboard.LeaderboardModel.remove().where("playerHandle").in([playerHandle1, playerHandle2]).exec(function(err) {
			done();
		});
	});

	it("Add one new leaderboard row", function(done) {
		var promise = new mongoose.Promise();
		
		promise.then(function () {
			return leaderboard.updateScore(boardId1, playerHandle1, 1);
		}).then(function (err, leaderboardRow) {
			return leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle1
			}).exec();
		}).then(function (leaderboardRow) {
			expect(leaderboardRow.score).toBe(1);
			done();
		}).then(null, function(err) {
			expect(err).toBe(null);
			done();
		});
		
		promise.complete();
	});

	it("Add one leaderboard row, then replace the score for one user", function(done) {
		var promise = new mongoose.Promise();
		
		promise.then(function() {
			return leaderboard.updateScore(boardId1, playerHandle1, 4);
		}).then(function() {
			return leaderboard.updateScore(boardId1, playerHandle1, 6);
		}).then(function retrieveScore() {
			return leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle1
			}).exec();
		}).then(function(leaderboardRow) {
			expect(leaderboardRow.score).toBe(6);
			done();
		}).then(null, function(err) {
			expect(err).toBe(null);
			done();
		});
		
		promise.complete();
	});

	it("Add two leaderboard rows for the same user, different boards", function(done) {
		var promise = new mongoose.Promise();
		
		promise.then(function () {
			return leaderboard.updateScore(boardId1, playerHandle1, 4);
		}).then(function (err, leaderboardRow) {
			return leaderboard.updateScore(boardId2, playerHandle1, 5);
		}).then(function (err, leaderboardRow) {
			return leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle1
			}).exec();
		}).then(function (leaderboardRow) {
			expect(leaderboardRow.score).toBe(4);
			return null;
		}).then(function () {
			return leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId2,
				"playerHandle" : playerHandle1
			}).exec();
		}).then(function (leaderboardRow) {
			expect(leaderboardRow.score).toBe(5);
			done();
		}).then(null, function(err) {
			expect(err).toBe(null);
			done();
		});
		
		promise.complete();
	});
	
	it("Add two leaderboard rows for different users, same board", function(done) {
		var promise = new mongoose.Promise();
		
		promise.then(function () {
			return leaderboard.updateScore(boardId1, playerHandle1, 4);
		}).then(function () {
			return leaderboard.updateScore(boardId1, playerHandle2, 5);
		}).then(function () {
			return leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle1
			}).exec();
		}).then(function (leaderboardRow) {
			expect(leaderboardRow.score).toBe(4);
			return null;
		}).then(function (err) {
			return leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle2
			}).exec();
		}).then(function (leaderboardRow) {
			expect(leaderboardRow.score).toBe(5);
			done();
		}).then(null, function(err) {
			expect(err).toBe(null);
			done();
		});
		
		promise.complete();
	});
});
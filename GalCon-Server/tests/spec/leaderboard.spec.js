var leaderboard = require('../../modules/model/leaderboard'), Step = require('step');

describe("Leaderboard tests", function() {
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
		Step(function updateScore() {
			leaderboard.updateScore(boardId1, playerHandle1, 1, this);
		}, function retrieveScore(err, leaderboardRow) {
			leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle1
			}, this);
		}, function validateScore(err, leaderboardRow) {
			expect(err).toBe(null);
			expect(leaderboardRow.score).toBe(1);
			done();
		});
	});

	it("Add one leaderboard row, then increment the score for one user", function(done) {
		Step(function updateScore() {
			leaderboard.updateScore(boardId1, playerHandle1, 4, this);
		}, function updateScore(err, leaderboardRow) {
			leaderboard.updateScore(boardId1, playerHandle1, 6, this);
		}, function retrieveScore(err, leaderboardRow) {
			leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle1
			}, this);
		}, function validateScore(err, leaderboardRow) {
			expect(err).toBe(null);
			expect(leaderboardRow.score).toBe(10);
			done();
		});
	});

	it("Add two leaderboard rows for the same user, different boards", function(done) {
		Step(function updateScore() {
			leaderboard.updateScore(boardId1, playerHandle1, 4, this);
		}, function updateScore(err, leaderboardRow) {
			leaderboard.updateScore(boardId2, playerHandle1, 5, this);
		}, function retrieveScore(err, leaderboardRow) {
			leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle1
			}, this);
		}, function validateScore(err, leaderboardRow) {
			expect(err).toBe(null);
			expect(leaderboardRow.score).toBe(4);
			return null;
		}, function retrieveScore(err) {
			leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId2,
				"playerHandle" : playerHandle1
			}, this);
		}, function validateScore(err, leaderboardRow) {
			expect(err).toBe(null);
			expect(leaderboardRow.score).toBe(5);
			done();
		});
	});
	
	it("Add two leaderboard rows for different users, same board", function(done) {
		Step(function updateScore() {
			leaderboard.updateScore(boardId1, playerHandle1, 4, this);
		}, function updateScore(err, leaderboardRow) {
			leaderboard.updateScore(boardId1, playerHandle2, 5, this);
		}, function retrieveScore(err, leaderboardRow) {
			leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle1
			}, this);
		}, function validateScore(err, leaderboardRow) {
			expect(err).toBe(null);
			expect(leaderboardRow.score).toBe(4);
			return null;
		}, function retrieveScore(err) {
			leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle2
			}, this);
		}, function validateScore(err, leaderboardRow) {
			expect(err).toBe(null);
			expect(leaderboardRow.score).toBe(5);
			done();
		});
	});
});
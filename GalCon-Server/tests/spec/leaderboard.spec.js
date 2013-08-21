var leaderboard = require('../../modules/model/leaderboard'), Step = require('step');

describe("Leaderboard tests", function() {
	var boardId1 = "TEST_BOARD1";
	var boardId2 = "TEST_BOARD2";
	var playerHandle = "TEST_HANDLE";

	afterEach(function(done) {
		leaderboard.LeaderboardModel.remove({
			"playerHandle" : playerHandle
		}, function(err) {
			done();
		});
	});

	it("Add one new leaderboard row", function(done) {
		Step(function updateScore() {
			leaderboard.updateScore(boardId1, playerHandle, 1, this);
		}, function retrieveScore(err, leaderboardRow) {
			leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle
			}, this);
		}, function validateScore(err, leaderboardRow) {
			expect(err).toBe(null);
			expect(leaderboardRow.score).toBe(1);
			done();
		});
	});

	it("Add two leaderboard rows for the same user/board", function(done) {
		Step(function updateScore() {
			leaderboard.updateScore(boardId1, playerHandle, 4, this);
		}, function updateScore(err, leaderboardRow) {
			leaderboard.updateScore(boardId1, playerHandle, 6, this);
		}, function retrieveScore(err, leaderboardRow) {
			leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle
			}, this);
		}, function validateScore(err, leaderboardRow) {
			expect(err).toBe(null);
			expect(leaderboardRow.score).toBe(10);
			done();
		});
	});

	it("Add two leaderboard rows for the same user, different boards", function(done) {
		Step(function updateScore() {
			leaderboard.updateScore(boardId1, playerHandle, 4, this);
		}, function updateScore(err, leaderboardRow) {
			leaderboard.updateScore(boardId2, playerHandle, 5, this);
		}, function retrieveScore(err, leaderboardRow) {
			leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId1,
				"playerHandle" : playerHandle
			}, this);
		}, function validateScore(err, leaderboardRow) {
			expect(err).toBe(null);
			expect(leaderboardRow.score).toBe(4);
			return null;
		}, function retrieveScore(err) {
			leaderboard.LeaderboardModel.findOne({
				"boardId" : boardId2,
				"playerHandle" : playerHandle
			}, this);
		}, function validateScore(err, leaderboardRow) {
			expect(err).toBe(null);
			expect(leaderboardRow.score).toBe(5);
			done();
		});
	});
});
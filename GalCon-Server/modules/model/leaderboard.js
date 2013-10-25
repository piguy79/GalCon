var mongoose = require('./mongooseConnection').mongoose, 
	db = require('./mongooseConnection').db, 
	game = require('./game.js'),
	_ = require('underscore');

var leaderboardSchema = mongoose.Schema({
	boardId : "String",
	playerHandle : "String",
	score : "Number"
});

leaderboardSchema.set('toObject', {
	getters : true
});
leaderboardSchema.index({
	boardId : 1
});
leaderboardSchema.index({
	boardId : 1,
	playerHandle : 1
}, {
	unique : true
});

var VICTORY_LEVEL_DIFF_MODIFIER = 0.5;
var VICTORY_POINTS = 10;

var LeaderboardModel = db.model('Leaderboard', leaderboardSchema);

exports.findScore = function(boardId, playerHandle) {
	return LeaderboardModel.findOne({
		'boardId' : boardId,
		'playerHandle' : playerHandle
	}).exec();
}

exports.updateScore = function(boardId, playerHandle, newScore) {
	return LeaderboardModel.findOneAndUpdate({
		'boardId' : boardId,
		'playerHandle' : playerHandle
	}, {
		$set : {
			'score' : newScore
		}
	}, {
		'upsert' : true
	}).exec();
}

/**
 * Updates the overall and map leaderboards for each player list in 'players'.
 * Returns a promise but with no results.
 */
exports.calculateAndSave = function(mapKey, players, handleOfPlayerWhoWon) {
	var promise = new mongoose.Promise();
	promise.complete();

	var lastPromise = promise;
	players.forEach(function(player) {
		lastPromise = lastPromise.then(function() {
			var lastScorePromise = exports.findScore(mapKey, player.handle);

			return lastScorePromise.then(function(leaderboardRow) {
				return game.GameModel.find({
					'endGameInformation.winnerHandle' : player.handle
				}).exec();
			}).then(function(gamesWonByUser) {
				var scores = _.map(gamesWonByUser, function(game) {return game.endGameInformation ? game.endGameInformation.leaderboardScoreAmount : 0});
				var currentScore = _.reduce(scores, function(memo, num) { return memo + num}, 0);

				currentScore += gameResultPoints(handleOfPlayerWhoWon, player, players);
				currentScore += userRecordBonusPoints(player, currentScore);
				
				return exports.updateScore(mapKey, player.handle, currentScore);
			}).then(null, function(err) {
				throw new Error(err);
			});
		});
	});

	return lastPromise;
}

/**
 * Give the game winner a set number of points, plus a bonus for playing a higher level opponent.  Give the game loser 0 points for the game.
 */
var gameResultPoints = function(handleOfPlayerWhoWon, player, players) {
	if(handleOfPlayerWhoWon != player.handle) {
		return 0;
	}
	
	var levelDiff = 0;
	players.forEach(function(p) {
		if(p.handle != player.handle) {
			levelDiff += p.rankInfo.level - player.rankInfo.level;
		}
	});
	
	return Math.floor(VICTORY_POINTS + Math.max(0, levelDiff * VICTORY_LEVEL_DIFF_MODIFIER));
}

/**
 * Give the user a bonus based on their win percentage.  A perfect win percentage will double their score.
 */
var userRecordBonusPoints = function(player, currentScore) {
	if(player.wins + player.losses == 0) {
		return 0;
	}
	return Math.floor(currentScore * (player.wins / (player.wins + player.losses)));
}

exports.LeaderboardModel = LeaderboardModel;
var mongoose = require('./mongooseConnection').mongoose, db = require('./mongooseConnection').db, ObjectId = require('mongoose').Types.ObjectId;

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

var LeaderboardModel = db.model('Leaderboard', leaderboardSchema);

exports.findScore = function(boardId, playerHandle, callback) {
	LeaderboardModel.findOne({
		'boardId' : boardId,
		'playerHandle' : playerHandle
	}, function(err, leaderboardRow) {
		if (err) {
			console.log("Error finding leaderboard: " + boardId + " for handle: " + playerHandle + ". Error: " + err);
			callback(null);
		} else {
			callback(leaderboardRow);
		}
	});
}

exports.updateScore = function(boardId, playerHandle, incrementScoreByAmount, callback) {
	exports.findScore(boardId, playerHandle, function(leaderboardRow) {
		if (!leaderboardRow) {
			var newRow = new LeaderboardModel({
				'boardId' : boardId,
				'playerHandle' : playerHandle,
				'score' : 0
			});
			newRow.save(function(err, leaderboardRow) {
				performUpdateScore(boardId, playerHandle, incrementScoreByAmount, 1, callback);
			});
		} else {
			performUpdateScore(boardId, playerHandle, incrementScoreByAmount, 1, callback);
		}
	});
}

var performUpdateScore = function(boardId, playerHandle, incrementScoreByAmount, attemptNumber, callback) {
	exports.findScore(boardId, playerHandle, function(leaderboardRow) {
		var oldScore = leaderboardRow.score;
		var newScore = oldScore + incrementScoreByAmount;

		LeaderboardModel.findOneAndUpdate({
			_id : leaderboardRow._id,
			score : oldScore
		}, {
			'score' : newScore
		}, function(err, updatedLeaderboardRow) {
			if (err || attemptNumber > 5) {
				console.log("Error [ " + err + "], attemptNumber + " + attemptNumber + ", updating leaderboard: "
						+ updatedLeaderboardRow);
				callback(null);
			} else if (!updatedLeaderboardRow) {
				performUpdateScore(boardId, playerHandle, incrementScoreByAmount, attemptNumber++, callback);
			} else {
				callback(updatedLeaderboardRow);
			}
		});
	});
}

exports.LeaderboardModel = LeaderboardModel;
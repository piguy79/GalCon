var mongoose = require('./mongooseConnection').mongoose, 
	db = require('./mongooseConnection').db, 
	gameManager = require('./game.js'),
	userManager = require('./user'),
	rankManager = require('./rank.js'),
	mapManager = require('./map.js'),
	_ = require('underscore');

var leaderboardSchema = mongoose.Schema({
	id : "String",
	user : {
		id: {type: mongoose.Schema.ObjectId, ref: 'User'},
		handle : "String"
	},
	score : "Number",
	rank : "Number",
	record : {
		w : "Number",
		l : "Number"
	}
});

leaderboardSchema.set('toObject', {
	getters : true
});
leaderboardSchema.index({
	id : 1
});
leaderboardSchema.index({
	id : 1,
	"user.id" : 1
}, {
	unique : true
});

var LeaderboardModel = db.model('Leaderboard', leaderboardSchema);

var winPercent = function(map) {
	var p = userManager.UserModel.find({ $or : [{ wins : { $gt : 0}}, {losses : {$gt : 0}}], handle : {$ne : 'AI'}},
	                                   {handle:1, _id:1, wins:1, losses:1}).setOptions({lean:true}).exec();
	var userWinLossMap = {};
	
	var DaysAgoWindowInMillis = 1000 * 60 * 60 * 24 * 180;
	var DaysAgoWindow = Date.now() - DaysAgoWindowInMillis;
	
	var maxGamesPlayed = 0;
	
	var gameQuery = {
		'endGame.winnerHandle' : {$exists : true, $ne : ''},
		'endGame.date' : {$gt : DaysAgoWindow},
		'ai' : {$ne : true},
		'round.num' : {$gte : 5}
	};
	
	if(map !== 'all') {
		_.extend(gameQuery, {map : map});
	}
	
	return p.then(function(users) {
		var innerp = new mongoose.Promise();
		innerp.fulfill();
		_.each(users, function(user) {
			userWinLossMap[user._id] = {w:0, l:0};
			innerp = innerp.then(function() {
				return gameManager.GameModel.find(
						_.extend(_.clone(gameQuery), {players : {$size : 2, $in : [user._id]},}), 
						{endGame:1, players:1, map:1})
					.setOptions({lean:true}).exec();
			}).then(function(games) {
				maxGamesPlayed = Math.max(maxGamesPlayed, games.length);
				_.each(games, function(game) {
					if(game.endGame.winnerHandle === user.handle) {
						recordWinLoss(userWinLossMap, user._id, 'w');
					} else {
						recordWinLoss(userWinLossMap, user._id, 'l');
					}
				});
			});
		});
		
		return innerp.then(function() {
			var calcP = new mongoose.Promise();
			calcP.fulfill();
			_.each(users, function(user) {
				calcP = calcP.then(function() {
					return gameManager.GameModel.find(
							_.extend(_.clone(gameQuery), {players : {$size : 2, $in : [user._id]},}),
							{endGame:1, players:1, map:1})
						.setOptions({lean:true}).exec();
				}).then(function(games) {
					var gamesPlayed = games.length;
					var score = 0;
					_.each(games, function(game) {
						if(game.endGame.winnerHandle === user.handle) {
							var opponentId = game.players[0] === user._id ? user._id : game.players[1];
							var opponentRecord = userWinLossMap[opponentId];
							var bonus = 0.42;
							var opponentGamesPlayed = opponentRecord.w + opponentRecord.l;
							if(opponentGamesPlayed > 14) {
								bonus = opponentRecord.w / opponentGamesPlayed;
							}
							bonus = Math.max(0.3333, bonus);
							bonus = Math.min(0.6666, bonus);
							
							score += bonus;
						}
					});
					
					if(gamesPlayed > 0) {
						score = score / gamesPlayed;
						
						// Reduce score for those with < 25 games played on a linear scale
						if(gamesPlayed < 25) {
							var keepPercent = gamesPlayed / 25.0;
							score *= keepPercent;
						}
					}
					
					// Normalize scores to 100, based off a max of .6666
					score *= 50;
					
					var userRecord = userWinLossMap[user._id];
					return exports.updateScore(map, _.pick(user, "_id", "handle"), score, userRecord);
				});
			});
			
			return calcP;
		});
	}).then(function() {
		return LeaderboardModel.find({id:map}).sort({'score' : -1}).exec();
	}).then(function(entries) {
		var innerp = new mongoose.Promise();
		innerp.fulfill();
		var lastRank = 0, lastScore = -1, countAtRank = 1;
		_.each(entries, function(entry) {
			var userRank;
			if(entry.score === lastScore) {
				countAtRank += 1;
				userRank = lastRank;
			} else {
				lastRank += countAtRank;
				countAtRank = 1;
				userRank = lastRank;
			}
			lastScore = entry.score;
			
			innerp = innerp.then(function() {
				entry.rank = userRank;
				return entry.withPromise(entry.save);
			})
		});
		
		return innerp;
	});
}

var recordWinLoss = function(userWinLossMap, id, winOrLossField) {
	var winLossMap = userWinLossMap[id];
	var field = winLossMap[winOrLossField];
	winLossMap[winOrLossField] = field + 1;
	userWinLossMap[id] = winLossMap;
}

exports.calculate = function() {
	var p = winPercent("all");
	return p.then(function() {
		return mapManager.MapModel.find().exec();
	}).then(function(maps) {
		var mapP = new mongoose.Promise();
		mapP.fulfill();
		
		_.each(maps, function(map) {
			mapP = mapP.then(function() {
				return winPercent(map.key);
			});
		});
		return mapP;
	});
}

exports.findTopScores = function(id, count, handle, limitToIds) {
	var query = {'id' : id, 'score' : {$gt : 0}};
	if(limitToIds) {
		_.extend(query, {'user.id' : {$in : limitToIds}});
	}
	
	var p = LeaderboardModel.find(query).sort({'score' : -1}).limit(count).exec();
	return p.then(function(entries) {
		if(!handle) {
			return entries;
		}
		
		var userp = LeaderboardModel.findOne({'id' : id, 'user.handle' : handle}).exec();
		return userp.then(function(userEntry) {
			if(userEntry != null) {
				entries.push(userEntry);
			}
			return entries;
		});
	});
}

exports.findScore = function(id, handle) {
	return LeaderboardModel.findOne({
		'id' : id,
		'user.handle' : handle
	}).exec();
}

exports.updateScore = function(id, user, score, record) {
	console.log("ID: " + id + ", Score: " + score + ", User: %j", user);
	return LeaderboardModel.findOneAndUpdate({
		'id' : id,
		'user.id' : user._id
	}, {
		$set : {
			'user.handle' : user.handle,
			'score' : score,
			'record' : record
		}
	}, {
		'upsert' : true
	}).exec();
}

exports.LeaderboardModel = LeaderboardModel;
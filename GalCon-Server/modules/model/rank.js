var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
,ObjectId = require('mongoose').Types.ObjectId;

var rankSchema = mongoose.Schema({
	level : "Number",
	startFrom : "Number",
	endAt : "Number"
});

rankSchema.set('toObject', { getters: true });
rankSchema.index({level : 1});

var RankModel = db.model('Rank', rankSchema);

exports.findRankForXp = function(xp){
	return RankModel.findOne({startFrom : {$lte : xp }, endAt : {$gt : xp}}).exec();
}

exports.findRankByName = function(rankName){
	return RankModel.findOne({level : rankName}).exec();
}

exports.findAllRanks = function(){
	return RankModel.find().sort({level : 1}).exec();
}

exports.findRankForAnXp = function(ranks, xp){	
	var matchingRank = _.filter(ranks, function(rank){
		return rank.startFrom <= xp && rank.endAt > xp;
	})
	
	return matchingRank[0];
}

exports.findRankRange = function(ranks, level){
	var maxRank = ranks[ranks.length-1];
	var range = 5;
	var lowerBound = level - range > 1 ? level-range : 2;
	var upperBound = level + range <= maxRank.level ? level + range : maxRank.level; 
	
	return {lowerBound : ranks[lowerBound-1], upperBound : ranks[upperBound-1]};
	
}


exports.RankModel = RankModel;
var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
,ObjectId = require('mongoose').Types.ObjectId;

var populateDefaultRanks = function(){
	var defaultRanks = require('./seed/ranks.json');
			
	for(var  i = 0 ; i < defaultRanks.ranks.length; i++){
		var rank = new RankModel(defaultRanks.ranks[i]);
		rank.save();
	}
}

var rankSchema = mongoose.Schema({
	level : "Number",
	startFrom : "Number",
	endAt : "Number"
});

rankSchema.set('toObject', { getters: true });
rankSchema.index({level : 1});

var RankModel = db.model('Rank', rankSchema);


RankModel.remove(function(err, doc) {
	if(err) {
		console.error("Could not delete ranks");
	} else {
		populateDefaultRanks();		
	}
});

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

exports.RankModel = RankModel;
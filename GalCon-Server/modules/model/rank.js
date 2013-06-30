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

exports.saveRank = function(rank, callback){
	rank.save(function(err, savedRank){
		callback(savedRank);
	});
}


exports.findRankForXp = function(xp, callback){
	RankModel.findOne({startFrom : {$lte : xp }, endAt : {$gt : xp}}, function(err, rank){
		if(err){
			console.log("Unable to find a rank for xp: " + xp);
		}else{
			callback(rank);
		}
	});
}

exports.findRankByName = function(rankName, callback){
	RankModel.findOne({level : rankName}, function(err, rank){
		if(err){
			console.log("Unable to find Rank information");
		}else{
			callback(rank);
		}
	});
}

exports.RankModel = RankModel;
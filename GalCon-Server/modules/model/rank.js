var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
,ObjectId = require('mongoose').Types.ObjectId;

var rankSchema = mongoose.Schema({
	name : "String",
	startFrom : "Number",
	endAt : "Number"
});

rankSchema.set('toObject', { getters: true });
rankSchema.index({name : 1});

var RankModel = db.model('Rank', rankSchema);


exports.saveRank = function(rank, callback){
	rank.save(function(err, savedRank){
		callback(savedRank);
	});
}


exports.findRankForXp = function(xp, callback){
	RankModel.findOne({startFrom : {$lt : xp }, endAt : {$gt : xp}}, function(err, rank){
		if(err){
			console.log("Unable to find a rank for xp: " + xp);
		}else{
			callback(rank);
		}
	});
}

exports.findRankByName = function(rankName, callback){
	RankModel.findOne({name : rankName}, function(err, rank){
		if(err){
			console.log("Unable to find Rank information");
		}else{
			callback(rank);
		}
	});
}

exports.RankModel = RankModel;
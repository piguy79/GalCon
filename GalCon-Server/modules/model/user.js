var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
,ObjectId = require('mongoose').Types.ObjectId;

var userSchema = mongoose.Schema({
	name : "String",
	handle : "String",
	createdDate : "Date",
	xp : "Number",
	wins : "Number",
	losses : "Number",
	currentGames : ["String"],
	coins : "Number",
	usedCoins : "Number",
	rankInfo : {
		level : "Number",
		startFrom : "Number",
		endAt : "Number"
	}
});

userSchema.set('toObject', { getters: true });
userSchema.index({name : 1});
userSchema.index({handle: 1});

userSchema.methods.createOrAdd = function(gameId, callback){
	this.model('User').update({name : this.name}, {$set : {name : this.name}, $pushAll : {currentGames : [gameId]}}, {upsert : true}, function(err){
		if(err){
			console.log("An Error Occured: " + err);
		}
		callback();
	});
}


var UserModel = db.model('User', userSchema);


exports.saveUser = function(user, callback){
	user.save(function(err, savedUser){
		callback(savedUser);
	});
}

exports.findUserByName = function(userName, callback){
	UserModel.findOne({name : userName}).exec(function(err, user){
		if(err){
			console.log("Unable to find User information");
		}else{
			callback(user);
		}
	});
}

exports.findUserByHandle = function(handle, callback){
	UserModel.findOne({handle : handle}).exec(function(err, user){
		if(err) {
			callback();
		} else {
			callback(user);
		}
	});
}

exports.addCoins = function(coinsToAdd, handle, usedCoins, callback){
	UserModel.findOneAndUpdate({ $and : [{handle : handle}, {usedCoins : usedCoins}]}, {$inc : {coins : coinsToAdd}, $set : {usedCoins : -1}}, function(err, user){
		if(err){
			callback({error : "Stale record"});
		}
		callback(user);
	});
}

exports.reduceTimeForWatchingAd = function(handle, usedCoins, reduceBy, callback){
	var timeToReduce = reduceBy * 60 * 1000;
	var reducedTime = usedCoins - timeToReduce;
	UserModel.findOneAndUpdate({$and : [{handle : handle}, {usedCoins : usedCoins}]}, {$set : {usedCoins : reducedTime}},function(err, user){
		if(err){
			console.log("ERROR: " + err);
			callback({error : "Unable to add time used count"});
		}else {
			callback(user);
		}
	});
}

exports.UserModel = UserModel;
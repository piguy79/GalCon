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
	watchedAd : "Boolean",
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

exports.findUserByHandle = function(handle){
	return UserModel.findOne({"handle" : handle}).exec();
}

exports.addCoins = function(coinsToAdd, handle, usedCoins){
	return UserModel.findOneAndUpdate({ $and : [{handle : handle}, {usedCoins : usedCoins}]}, {$inc : {coins : coinsToAdd}, $set : {usedCoins : -1, watchedAd : false}}).exec();
}

exports.reduceTimeForWatchingAd = function(handle, usedCoins, timeRemaining, reduceBy){
	var reducedTime = Math.floor(usedCoins - (timeRemaining * reduceBy));
	if(reducedTime < 0){
		reducedTime = -1;
	}
	return UserModel.findOneAndUpdate({$and : [{handle : handle}, {usedCoins : usedCoins}, {watchedAd : false}]}, {$set : {usedCoins : reducedTime, watchedAd : true}}).exec();
}

exports.UserModel = UserModel;
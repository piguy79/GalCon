var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
,ObjectId = require('mongoose').Types.ObjectId;

var userSchema = mongoose.Schema({
	email : "String",
	handle : "String",
	createdDate : "Date",
	xp : "Number",
	wins : "Number",
	losses : "Number",
	session : {
		id : "String",
		expireDate : "Date"
	},
	currentGames : [{type: mongoose.Schema.ObjectId, ref: 'Game'}],
	consumedOrders : [{
		orderId : "String",
		packageName : "String",
	    productId : "String",
	    purchaseTime : "String",
	    purchaseState : "String",
	    developerPayload : "String",
	    token : "String",
	    associatedCoins : "Number"
	}],
	auth : [String],
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
userSchema.index({email : 1});
userSchema.index({handle: 1});
userSchema.index({"sessions.sessionId" : 1});

var UserModel = db.model('User', userSchema);

exports.findUserByEmail = function(email) {
	return UserModel.findOne({email : email}).exec();
}

exports.findUserByHandle = function(handle){
	return UserModel.findOne({"handle" : handle}).exec();
}

exports.findUserWithGames = function(handle){
	return UserModel.findOne({"handle" : handle}).populate('currentGames').exec();
}

exports.addCoins = function(coinsToAdd, handle){
	return UserModel.findOneAndUpdate({ 
										handle : handle
										},
										{
											$inc : 
													{
														coins : coinsToAdd
													}, 
	                                          $set : 
	                                          		{
	                                        	  		usedCoins : -1,
	                                        	  		watchedAd : false
	                                        	  	}
										}).exec();
}

exports.addCoinsForAnOrder = function(handle, order){
	return UserModel.findOneAndUpdate(
										{ 
											handle : handle, 
											'consumedOrders.orderId' : 
													 					{
														 					$nin : [order.orderId]
													 					}
										}, 
										{
											$inc : 
													{
														coins : order.associatedCoins
													}, 
											$set : 
													{
														usedCoins : -1,
														watchedAd : false
													}, 
											$push : {
														consumedOrders : order
													}
										}).exec();
}

exports.deleteConsumedOrder = function(handle, order){
	return UserModel.findOneAndUpdate({handle : handle}, {$pull : {consumedOrders : {'orderId' : order.orderId}}}).exec();
}

exports.reduceTimeForWatchingAd = function(handle, config){
	var p = exports.findUserByHandle(handle);
	return p.then(function(user) {
		var timeReduction = config.values['timeReduction'];
		var timeLapseForNewCoins = config.values['timeLapseForNewCoins'];
		var timeRemaining = user.usedCoins + timeLapseForNewCoins - Date.now();
		var reducedTime = Math.floor(timeRemaining * timeReduction);
		var updatedUsedCoins;
		if(reducedTime < 0) {
			updatedUsedCoins = -1;
		} else {
			updatedUsedCoins = user.usedCoins + reducedTime;
		}
		
		return UserModel.findOneAndUpdate({$and : [{handle : handle}, {watchedAd : false}]}, {$set : {usedCoins : updatedUsedCoins, watchedAd : true}}).exec();
	});
}

exports.updateUsedCoins = function(handle, usedCoins){
	return UserModel.findOneAndUpdate({handle : handle}, {$set : {usedCoins : usedCoins}}).exec();
}

exports.UserModel = UserModel;
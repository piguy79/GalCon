var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
,ObjectId = require('mongoose').Types.ObjectId;

var userSchema = mongoose.Schema({
	handle : "String",
	createdDate : "Date",
	xp : "Number",
	wins : "Number",
	losses : "Number",
	session : {
		id : "String",
		expireDate : "Date"
	},
	friends : [{
		user : {type: mongoose.Schema.ObjectId, ref: 'User'},
		played : "Number"
	}],
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
	auth : {
		google : "String",
		twitter : "String",
		facebook : "String"
	},
	coins : "Number",
	usedCoins : "Number",
	watchedAd : "Boolean"
});

userSchema.set('toObject', { getters: true });
userSchema.index({'auth.google' : 1});
userSchema.index({'auth.twitter' : 1});
userSchema.index({'auth.facebook' : 1});
userSchema.index({handle: 1});
userSchema.index({"sessions.sessionId" : 1});

var UserModel = db.model('User', userSchema);

exports.findUserById = function(id, authProvider) {
	var searchKey = 'auth.' + authProvider;
	var search = {};
	search[searchKey] = id;
	
	return UserModel.findOne(search).exec();
}

exports.findUserByHandle = function(handle){
	return UserModel.findOne({"handle" : handle}).populate('friends.user').exec();
}


exports.findUserMatchingSearch = function(searchTerm, handle){
	return UserModel.find({ $and : [{"handle" : new RegExp('^'+searchTerm+'.*', "i")}, {handle : {$ne : handle}}]}).limit(10).exec();
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

exports.joinAGame = function(user, game){
	return UserModel.findOneAndUpdate({$and : [{handle : user.handle}, {coins : {$gt : 0}}]}, 
			{
				$inc : {coins : -1}
			}).exec();
}

exports.removeAGame = function(user, gameId){
	return UserModel.findOneAndUpdate({handle : user.handle},
			{
				$inc : {coins : 1},
				$set : {usedCoins : -1}
			}).exec();
}

exports.updateFriend = function(user, updateFriend){
	var existingFriend =  _.filter(user.friends, function(friend){
		return friend.user && friend.user.handle === updateFriend.handle;
	});
	if(existingFriend && existingFriend.length > 0){
		return UserModel.update({handle : user.handle , 'friends.user' : updateFriend._id } , 
                {$inc : {'friends.$.played' : 1} }).exec();
	}else{
		return UserModel.findOneAndUpdate({handle : user.handle}, {$push : {friends : {user : updateFriend, played :  1}}}).exec();
	}
}

exports.UserModel = UserModel;
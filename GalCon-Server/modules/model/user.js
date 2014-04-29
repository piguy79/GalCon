var mongoose = require('./mongooseConnection').mongoose,
	db = require('./mongooseConnection').db,
	ObjectId = require('mongoose').Types.ObjectId,
	inventory = require('./inventory');

var userSchema = mongoose.Schema({
	handle : "String",
	createdDate : "Date",
	xp : "Number",
	wins : "Number",
	losses : "Number",
	na : "Boolean",
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
	coins : "Number"
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

exports.addCoins = function(coins, handle) {
	return UserModel.findOneAndUpdate({handle : handle}, {$inc : {coins : coins}}).exec();
}

exports.addCoinsForAnOrder = function(handle, order) {
	var p = inventory.InventoryModel.findOne({'sku' : order.productId}).exec();
	return p.then(function(inventoryItem) {
		if(!inventoryItem) {
			throw new Error("Could not find inventory item for: " + order.productId); 
		}
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
														coins : inventoryItem.associatedCoins
													}, 
											$set :  {
														na : true
											},
											$push : {
														consumedOrders : order
													}
										}).exec();
	});
}

exports.deleteConsumedOrder = function(handle, order){
	return UserModel.findOneAndUpdate({handle : handle}, {$pull : {consumedOrders : {'orderId' : order.orderId}}}).exec();
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
				$inc : {coins : 1}
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
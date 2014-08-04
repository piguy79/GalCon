var validator = require("validator"),
	_ = require("underscore"),
	gameManager = require('./model/game');

exports.isSession = function(session) {
	if(session === undefined || session == null) {
		return false;
	}
	
	if(session.length != 64) {
		return false;
	}
	
	var newSession = session.replace(/[^A-Za-z0-9]/g, '');
	
	if(newSession != session) {
		return false;
	}
	
	return true;
}

exports.isOS = function(os) {
	return validator.equals(os, "android") || validator.equals(os, "ios");
}

exports.isEmail = function(email) {
	return validator.isLength(email, 5, 100) && validator.isEmail(email);	
}

exports.isMongoId = function(id) {
	return validator.isLength(id, 16, 32) && validator.isAlphanumeric(id);
}

exports.isMapKey = function(mapKey) {
	try {
		validator.isLength(mapKey, 1, 4);
		validator.isInt(mapKey);
	} catch(e) {
		return false;
	}
	
	return true;
}

exports.isLeaderboard = function(leaderboard) {
	if(leaderboard === 'all') {
		return true;
	}
	
	return exports.isMapKey(leaderboard);
}

exports.isMapVersion = function(version) {
	try {
		validator.isLength(version, 1, 4);
		validator.isInt(version);
	} catch(e) {
		return false;
	}
	
	return true;
}

exports.isOrders = function(orders) {
	
	if(!_.isArray(orders)) {
		return false;
	}
		
	var errorFound = _.some(orders, function(order){
		return !validator.isLength(order.orderId, 1, 200) &&
		!validator.isLength(order.packageName, 1, 200) &&
		!validator.isLength(order.productId, 1, 200) &&
		!validator.isLength(order.purchaseTime, 1, 200) &&
		!validator.isLength(order.purchaseState, 1, 200) &&
		!validator.isLength(order.developerPayload, 0, 200) &&
		!validator.isLength(order.token, 1, 200);
	});
		
	if(errorFound){
		return false;
	}
	
	return true;
}

exports.isHandle = function(handle) {
	var updatedHandle = handle.replace(/^\s+/, '');
	updatedHandle = updatedHandle.replace(/\s+$/, '');
	updatedHandle = updatedHandle.replace(/[^a-z0-9_]/i);
	
	if (handle.length < 3 || handle.length > 16 || updatedHandle !== handle) {
		return false;
	}
	
	return true;
}

exports.isValidMoves = function(arg){
	var moves = arg['moves'];
	var handle = arg['handle'];
	
	if(!moves){
		return true;
	}

	
	if(!_.isArray(moves)){
		return false;
	}
		
	return !_.some(moves, function(move){
		return move.from === move.to || move.handle !== handle || !validator.isInt(move.fleet) || move.fleet <= 0;
	});
	
}

exports.isAuthProvider = function(authProvider){
	var validAuthProviders = ['google', 'facebook'];
	
	return _.contains(validAuthProviders, authProvider);
}

exports.isSocialId = function(id){
	return id.length < 100;
}

exports.isSocialIdGroup = function(ids){
	return _.every(ids, exports.isSocialId);
}

exports.isToken = function(token){
	return token.length < 500;
}



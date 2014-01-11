var validator = require("validator"),
	_ = require("underscore");

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

exports.isEmail = function(email) {
	try {
		validator.check(email).len(5, 100).isEmail();
	} catch(e) {
		return false;
	}
	
	return true;
}

exports.isMapKey = function(mapKey) {
	try {
		validator.check(mapKey).len(1, 4).isInt();
	} catch(e) {
		return false;
	}
	
	return true;
}

exports.isOrders = function(orders) {
	try {
		if(!_.isArray(orders)) {
			return false;
		}
		_.each(orders, function(order) {
			validator.check(order.orderId).len(1, 200);
			validator.check(order.packageName).len(1, 200);
			validator.check(order.productId).len(1, 200);
			validator.check(order.purchaseTime).len(1, 200);
			validator.check(order.purchaseState).len(1, 200);
			validator.check(order.developerPayload).len(0, 200);
			validator.check(order.token).len(1, 200);
		});
	} catch(e) {
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
var validator = require("validator"),
	_ = require("underscore"),
	mongoose = require('./model/mongooseConnection').mongoose,
	userManager = require('./model/user');


exports.validate = function(user, inviteeHandle){	
	return runValidate(user, inviteeHandle)
}

var runValidate = function(user, inviteeHandle){
	if(!user){
		return false;
	}

	return userHasCoins(user) && requesterAndInviteeAreDifferent(user.handle, inviteeHandle);
}

var userHasCoins = function(user){
	return user.coins > 0;
}

var requesterAndInviteeAreDifferent = function(requesterHandle, inviteeHandle){
	return requesterHandle !== inviteeHandle;
}



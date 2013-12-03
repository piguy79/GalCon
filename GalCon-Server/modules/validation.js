var validator = require("validator");

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

exports.isHandle = function(handle) {
	var updatedHandle = handle.replace(/^\s+/, '');
	updatedHandle = updatedHandle.replace(/\s+$/, '');
	updatedHandle = updatedHandle.replace(/[^a-z0-9_]/i);
	
	if (handle.length < 3 || handle.length > 16 || updatedHandle !== handle) {
		return false;
	}
	
	return true;
}
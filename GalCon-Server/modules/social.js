var googleapis = require('googleapis'),
	mongoose = require('mongoose'),
	crypto = require('crypto'),
	userManager = require('./model/user');

var isValid = function(authProvider, token) {
	if (authProvider !== "google") {
		console.log("Invalid auth provider: " + authProvider);
		return false;
	}

	if (token == null || token.length > 500) {
		console.log("Invalid token: " + token);
		return false;
	}
	return true;
}

exports.exchangeToken = function(authProvider, token) {
	var p = new mongoose.Promise();
	p.complete();
	
	var email;
	
	return p.then(function() {
		if(!isValid(authProvider, token)) {
			throw new Error("Invalid request");
		}
	}).then(function() {
		var gapiP = new mongoose.Promise();
		googleapis
			.discover('plus', 'v1')
			.execute(function(err, client) {
			if(err) {
				gapiP.reject(err.message);
			} else {
				gapiP.complete(client);
			}
		});
		
		return gapiP;
	}).then(function(client) {
		var gapiP = new mongoose.Promise();
		var oAuthClient = new googleapis.OAuth2Client();
		oAuthClient.credentials = {};
		oAuthClient.credentials.access_token = token;
		client.plus.people
			.get({userId: "me"})
			.withAuthClient(oAuthClient)
			.execute(function(err, result) {
				if(err) {
					gapiP.reject(err.message);
				} else {
					if(result.emails) {
						result.emails.forEach(function(emailObj) {
							if(emailObj.type === "account") {
								email = emailObj.value;
							}
						})
					}
					if(email === undefined || email.length < 3) {
						gapiP.reject("Unable to find email address");
					} else {
						gapiP.complete();
					}
				}
			});
		return gapiP;
	}).then(function() {
		return userManager.UserModel.findOneAndUpdate({email : email}, {$set : {session : {}}}).exec();
	}).then(function() {
		var sha = crypto.createHash('sha256');
		sha.update(Math.random().toString() + "randomseedinformation blash4tgjasdffsdfsafwpooiommlejefkwf" + Date.now().toString());
		return sha.digest('hex');
	}).then(function(session) {
		var sessionObj = {
			id : session,
			expireDate : Date.now() + 4 * 60 * 60 * 1000
		};
		return userManager.UserModel.findOneAndUpdate({email : email}, {$set : {email : email, session : sessionObj}}, {upsert: true}).exec();
	}).then(function(user) {
		return user.sessions[0].sessionId;
	});
}
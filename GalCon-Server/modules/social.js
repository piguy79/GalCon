var googleapis = require('googleapis'),
	facebook = require('facebook-api'),
	mongoose = require('mongoose'),
	crypto = require('crypto'),
	userManager = require('./model/user'),
	rankManager = require('./model/rank'),
	socialProviders = require('./socialProviders');

var authIdRequest = {
		google : socialProviders.authIdFromGoogle
};

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
	
	var authId;
	
	return p.then(function() {
		if(!isValid(authProvider, token)) {
			throw new Error("Invalid request");
		}
	}).then(function() {
		console.log(authIdRequest);
		return authIdRequest[authProvider].call(this, token);
	}).then(function(authId) {
		authId = authId;
		return userManager.UserModel.findOneAndUpdate({authId : authId} ,{ $set : {session : {}}}).exec();
	}).then(function(user) {
		if(user === null) {
			user = new userManager.UserModel({
				authId : authId,
				currentGames : [],
				xp : 0,
				wins : 0,
				losses : 0,
				coins : 10,
				usedCoins : -1,
				watchedAd : false
			});
			var innerp = rankManager.findRankByName("1");
			return innerp.then(function(rank) {
				user.rankInfo = rank;
				return user.withPromise(user.save);
			});
		}
	}).then(function() {
		var sha = crypto.createHash('sha256');
		sha.update(Math.random().toString() + "randomseedinformation blash4tgjasdffsdfsafwpooiommlejefkwf" + Date.now().toString());
		return sha.digest('hex');
	}).then(function(session) {
		var sessionObj = {
			id : session,
			expireDate : Date.now() + 4 * 60 * 60 * 1000
		};
		return userManager.UserModel.findOneAndUpdate({authId : authId}, {$set : {authId : authId, session : sessionObj}}, {upsert: true}).exec();
	}).then(function(user) {
		return user.session.id;
	});
}




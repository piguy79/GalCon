var googleapis = require('googleapis'),
	facebook = require('facebook-api'),
	mongoose = require('mongoose'),
	crypto = require('crypto'),
	userManager = require('./model/user'),
	rankManager = require('./model/rank');

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
		return authIdRequest[authProvider].call(this, token);
	}).then(function(authId) {
		var authId = authId;
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

var authIdRequest = {
		google : authIdFromGoogle
}

var authIdFromGoogle = function(token){
	var returnP = new mongoose.Promise();
	
	var gapiP = new mongoose.Promise();
	gapiP.complete();
	gapiP.then(function(){
		var discoverP = new mongoose.Promise();
		googleapis
			.discover('plus', 'v1')
			.execute(function(err, client) {
			if(err) {
				discoverP.reject(err.message);
			} else {
				discoverP.complete(client);
			}
		});
		
		return discoverP;
	}).then(function(client){
		var authP = new mongoose.Promise();
		var oAuthClient = new googleapis.OAuth2Client();
		oAuthClient.setCredentials({
			  access_token: token
			});
		client.plus.people
			.get({userId: "me"})
			.withAuthClient(oAuthClient)
			.execute(function(err, result) {
				if(err) {
					console.log("Google Plus API - Error - %j", err);
					authP.reject(err.message);
				} else {
					if(authId === undefined){
						authP.reject("Unable to load ID");
					} else {
						authP.complete(result.id);
					}
				}
			});
		return authP;
	}).then(function(authId){
		returnP.complete(authId);
	}, function(err){
		returnP.reject(err);
	});
	
	return returnP;
}
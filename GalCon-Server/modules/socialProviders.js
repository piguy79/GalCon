var googleapis = require('googleapis'),
	facebook = require('facebook-api'),
	mongoose = require('mongoose'),
	crypto = require('crypto'),
	userManager = require('./model/user'),
	rankManager = require('./model/rank');


exports.authIdFromGoogle = function(token){
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
};
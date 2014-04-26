var googleapis = require('googleapis'),
	facebook = require('facebook-api'),
	mongoose = require('mongoose'),
	crypto = require('crypto'),
	userManager = require('./model/user'),
	rankManager = require('./model/rank');


exports.authIdFromGoogle = function(token){
	var returnP = new mongoose.Promise();
	
	var gapiP = new mongoose.Promise();
	gapiP.fulfill();
	gapiP.then(function(){
		var discoverP = new mongoose.Promise();
		googleapis
			.discover('plus', 'v1')
			.execute(function(err, client) {
			if(err) {
				discoverP.reject(err.message);
			} else {
				discoverP.fulfill(client);
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
					if(result.id === undefined){
						authP.reject("Unable to load ID");
					} else {
						authP.fulfill(result.id);
					}
				}
			});
		return authP;
	}).then(function(authId){
		returnP.fulfill(authId);
	}, function(err){
		returnP.reject(err);
	});
	
	return returnP;
};

exports.authIdFromFacebook = function(token){
	var returnP = new mongoose.Promise();
	
	var clientP = new mongoose.Promise();
	clientP.fulfill();
	clientP.then(function(){
		var client = facebook.user(token);
		client.me.info(function(err, data){
			if(err){
				returnP.reject("Unable to load ID");
			}else{
				returnP.fulfill(data.id);
			}
		});
	});
	return returnP;
	
}
var needle = require("needle"), 
	apiRunner = require('../fixtures/apiRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	userManager = require('../../modules/model/user'),
	configManager = require('../../modules/model/config'),
	mongoose = require('mongoose'),
	googleapis = require('googleapis');

describe("Add Free Coins", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	PLAYER_1.coins = 0;
	PLAYER_1.usedCoins = Date.now() - (1000 * 60 * 60 * 12);
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 2);
	PLAYER_2.coins = 1;
	PLAYER_2.usedCoins = -1;
	
	var PLAYER_3_HANDLE = "TEST_PLAYER_3";
	var PLAYER_3 = elementBuilder.createUser(PLAYER_3_HANDLE, 3);
	PLAYER_3.coins = 0;
	PLAYER_3.usedCoins = Date.now();

	beforeEach(function(done) {
		var p = userManager.UserModel.withPromise(userManager.UserModel.create, [PLAYER_1, PLAYER_2, PLAYER_3]);
		p.then(function(){
			done();
		});		
	});
	
	afterEach(function(done) {
		userManager.UserModel.remove().where("handle").in([PLAYER_1_HANDLE, PLAYER_2_HANDLE, PLAYER_3_HANDLE]).exec(function(err) {
			if(err) { console.log(err); }
			done();
		});
	});
	
	it("Add free coins when time is expired and coins are zero", function(done) {
		var p = apiRunner.addFreeCoins(PLAYER_1_HANDLE, PLAYER_1.session.id);
		p.then(function(player) {
			var innerp = configManager.findLatestConfig('app');
			return innerp.then(function(config) {
				expect(player.coins).toBe(config.values['freeCoins']);
				expect(player.usedCoins).toBe(-1);
				expect(player.watchedAd).toBe(false);
			});
		}).then(done, done);
	});
	
	it("Attempt to add free coins when coins are still available should fail to update the user", function(done) {
		var p = apiRunner.addFreeCoins(PLAYER_2_HANDLE, PLAYER_2.session.id);
		p.then(function(player) {
			var innerp = configManager.findLatestConfig('app');
			return innerp.then(function(config) {
				expect(player.coins).toBe(1);
				expect(player.usedCoins).toBe(-1);
				expect(player.watchedAd).toBe(false);
			});
		}).then(done, done);
	});
	
	it("Attempt to add free coins when timeout has not expired should fail to update the user", function(done) {
		var p = apiRunner.addFreeCoins(PLAYER_3_HANDLE, PLAYER_3.session.id);
		p.then(function(player) {
			var innerp = configManager.findLatestConfig('app');
			return innerp.then(function(config) {
				expect(player.coins).toBe(0);
				expect(player.usedCoins).toBeGreaterThan(0);
				expect(player.watchedAd).toBe(false);
			});
		}).then(done, done);
	});
	
	it("get token", function(done) {
		var clientId = "1066768766862-6nqm53i5ab34js3oo8jcv05bkookob87.apps.googleusercontent.com";
		var clientSecret = "2R4_ZmTBwBEAmEo7_W8hIyZn";
		var redirectUrl = "http://localhost";
		var oAuthClient = new googleapis.OAuth2Client(clientId, clientSecret, redirectUrl);
		oAuthClient.getToken("4/v727tjj6LJDFHyWkbyRF0gOeksgu.gla6LVVOqsofmmS0T3UFEsPspcH3hgI", function(err, tokens) {
			console.log(err);
			  console.log(tokens);
			  done();
			});
	});
});
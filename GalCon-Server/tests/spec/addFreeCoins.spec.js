var needle = require("needle"), 
	apiRunner = require('../fixtures/apiRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	userManager = require('../../modules/model/user'),
	configManager = require('../../modules/model/config'),
	mapManager = require('../../modules/model/map'),
	gameManager = require('../../modules/model/game'),
	mongoose = require('mongoose'),
	googleapis = require('googleapis');

describe("Add Free Coins", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	PLAYER_1.coins = 0;
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 2);
	PLAYER_2.coins = 1;
	
	var PLAYER_3_HANDLE = "TEST_PLAYER_3";
	var PLAYER_3 = elementBuilder.createUser(PLAYER_3_HANDLE, 3);
	PLAYER_3.coins = 1;
	
	var MAP_KEY_1 = -100;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);

	beforeEach(function(done) {
		var p = userManager.UserModel.withPromise(userManager.UserModel.create, [PLAYER_1, PLAYER_2, PLAYER_3]);
		p.then(function(){
			(new mapManager.MapModel(MAP_1)).save(function(err, map) {
				done();
			});
		});		
	});
	
	afterEach(function(done) {
		gameManager.GameModel.remove().where("map").in([MAP_KEY_1]).exec(function(err) {
			if(err) { console.log(err); }
			mapManager.MapModel.remove().where("key").in([MAP_KEY_1]).exec(function(err) {
				if(err) { console.log(err); }
				userManager.UserModel.remove().where("handle").in([PLAYER_1_HANDLE, PLAYER_2_HANDLE, PLAYER_3_HANDLE]).exec(function(err) {
					if(err) { console.log(err); }
					done();
				});
			});
		});
	});
	
	it("Add free coins when user has zero coins and no games are in progress", function(done) {
		var p = apiRunner.addFreeCoins(PLAYER_1_HANDLE, PLAYER_1.session.id);
		p.then(function(player) {
			var innerp = configManager.findLatestConfig('app');
			return innerp.then(function(config) {
				expect(player.coins).toBe(config.values['freeCoins']);
			});
		}).then(done, done);
	});
	
	it("Attempt to add free coins when coins are still available should fail to update the user", function(done) {
		var p = apiRunner.addFreeCoins(PLAYER_2_HANDLE, PLAYER_2.session.id);
		p.then(function(player) {
			var innerp = configManager.findLatestConfig('app');
			return innerp.then(function(config) {
				expect(player.coins).toBe(1);
			});
		}).then(done, done);
	});
	
	it("Attempt to add free coins when games are still in progress should not give the user credit for coins", function(done) {
		var p = apiRunner.matchPlayerToGame(PLAYER_3_HANDLE, MAP_KEY_1, PLAYER_3.session.id);
		p.then(function() {
			return apiRunner.addFreeCoins(PLAYER_3_HANDLE, PLAYER_3.session.id);
		}).then(function(player) {
			expect(player.coins).toBe(0);
		}).then(done, done);
	});
});
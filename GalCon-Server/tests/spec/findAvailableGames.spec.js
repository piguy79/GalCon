var needle = require("needle"), 
	apiRunner = require('../fixtures/apiRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	elementMatcher = require('../fixtures/elementMatcher'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	mongoose = require('mongoose');

describe("Find available games -", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 5);
	
	var PLAYER_3_HANDLE = "TEST_PLAYER_3";
	
	var MAP_KEY_1 = -100;
	var MAP_KEY_2 = -200;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);
	var MAP_2 = elementBuilder.createMap(MAP_KEY_2, 5, 6);

	beforeEach(function(done) {
		(new userManager.UserModel(PLAYER_1)).save(function(err, user) {
			if(err) { console.log(err); }
			(new userManager.UserModel(PLAYER_2)).save(function(err, user) {
				if(err) { console.log(err); }
				(new mapManager.MapModel(MAP_1)).save(function(err, map) {
					if(err) { console.log(err); }
					(new mapManager.MapModel(MAP_2)).save(function(err, map) {
						if(err) { console.log(err); }
						done();
					});
				});
			});
		});
	});
	
	afterEach(function(done) {
		gameManager.GameModel.remove().where("map").in([MAP_KEY_1, MAP_KEY_2]).exec(function(err) {
			if(err) { console.log(err); }
			mapManager.MapModel.remove().where("key").in([MAP_KEY_1, MAP_KEY_2]).exec(function(err) {
				if(err) { console.log(err); }
				userManager.UserModel.remove().where("handle").in([PLAYER_1_HANDLE, PLAYER_2_HANDLE]).exec(function(err) {
					if(err) { console.log(err); }
					done();
				});
			});
		});
	});

	it("Should not see games you created", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		p.then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1);
		}).then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_2_HANDLE, MAP_KEY_2);
		}).then(function() {
			return apiRunner.findAvailableGames(PLAYER_1_HANDLE);
		}).then(function(games) {
			var availableGames = games.items;
			expect(availableGames.length).toBe(1);
			expect(availableGames[0].players.length).toBe(1);
			expect(availableGames[0].players[0].handle).toBe(PLAYER_2_HANDLE);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});

	it("Should not see a game that already has two players", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		p.then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1);
		}).then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_2_HANDLE, MAP_KEY_1);
		}).then(function() {
			return apiRunner.findAvailableGames(PLAYER_3_HANDLE);
		}).then(function(games) {
			var availableGames = games.items;
			expect(availableGames.length).toBe(0);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
});

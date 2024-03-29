var needle = require("needle"), 
	apiRunner = require('../fixtures/apiRunner'), 
	gameRunner = require('../fixtures/gameRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	elementMatcher = require('../fixtures/elementMatcher'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	mongoose = require('mongoose');

describe("Resign game -", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 5);
	
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

	
	it("Should not be able to resign a game that you are not playing in", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		p.then(function() {
			return gameRunner.createGameAwaitingAccept(PLAYER_2, PLAYER_1, MAP_KEY_1);
		}).then(function(game) {
			return apiRunner.resignGame(game._id, PLAYER_1_HANDLE, PLAYER_1.session.id);
		}).then(function(response) {
			expect(response.error).toBe("Invalid game");
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
	
	it("Should be able to resign a game that you have joined", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		p.then(function() {
			return gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		}).then(function(game) {
			return apiRunner.resignGame(game._id, PLAYER_1_HANDLE, PLAYER_1.session.id);
		}).then(function(response) {
			expect(response.endGame.winnerHandle).toBe(PLAYER_2_HANDLE);
			
			response.players.forEach(function(player) {
				if(player.handle === PLAYER_1_HANDLE) {
					expect(player.losses).toBe(1);
				} else {
					expect(player.wins).toBe(1);
				}
			});
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
});

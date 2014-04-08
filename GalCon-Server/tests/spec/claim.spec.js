var needle = require("needle"), 
	apiRunner = require('../fixtures/apiRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	elementMatcher = require('../fixtures/elementMatcher'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	mongoose = require('mongoose'),
	gameRunner = require('../fixtures/gameRunner'),
	_ = require('underscore');

describe("Perform Move - Standard -", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	PLAYER_1.xp = 1;
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 1);
	
	var PLAYER_3_HANDLE = "TEST_PLAYER_3";
	var PLAYER_3 = elementBuilder.createUser(PLAYER_3_HANDLE, 1);
	
	var MAP_KEY_1 = -100;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);

	var PLAYER_1_HOME_PLANET = "HOME_PLANET_1";
	var PLAYER_2_HOME_PLANET = "HOME_PLANET_2";
	var UNOWNED_PLANET_1 = "UNOWNED_PLANET_1";
	var PLANETS = [ elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 50, { x : 3, y : 4}),
	                elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 3, 30, { x : 3, y : 4}),
	                              elementBuilder.createPlanet(UNOWNED_PLANET_1, "", 3, 20, { x : 3, y : 5}) ];
	
	beforeEach(function(done) {
		var p = userManager.UserModel.withPromise(userManager.UserModel.create, [PLAYER_1, PLAYER_2, PLAYER_3]);
		p.then(function(){
			return mapManager.MapModel.withPromise(mapManager.MapModel.create, [MAP_1]);
		}).then(function(){
			done();
		});		
	});
	
	afterEach(function(done) {
		gameManager.GameModel.remove().where("map").in([MAP_KEY_1]).exec(function(err) {
			if(err) { console.log(err); }
			mapManager.MapModel.remove().where("key").in([MAP_KEY_1]).exec(function(err) {
				if(err) { console.log(err); }
				userManager.UserModel.remove().where("handle").in([PLAYER_1_HANDLE, PLAYER_2_HANDLE]).exec(function(err) {
					if(err) { console.log(err); }
					done();
				});
			});
		});
	});

	it("Should allow a user to claim the game if it has gone past the timeout", function(done) {
		var currentGameId;
		var currentGame;
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		p.then(function(game){
			currentGameId = game._id;
			var moveTimeOlderThenTimeout = game.moveTime - parseInt(game.config.values['claimTimeout']);
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {moveTime: moveTimeOlderThenTimeout}}).exec();
		}).then(function(){
			return apiRunner.claimVictory(PLAYER_1.handle, currentGameId, PLAYER_1.session.id);
		}).then(function(claimedGame){
			return gameManager.findById(currentGameId);
		}).then(function(game){
			currentGame = game;
			expect(game.endGame.winnerHandle).toBe(PLAYER_1.handle);
			return userManager.findUserByHandle(PLAYER_1.handle);
		}).then(function(user){
			// Make sure the user gets the xp for a win.
			expect(user.xp).toBe(PLAYER_1.xp + parseInt(currentGame.config.values['xpForWinning']));
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	it("Should not allow claim if the timeout has not been reached", function(done) {
		var currentGameId;
		var currentGame;
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		p.then(function(){
			return apiRunner.claimVictory(PLAYER_1.handle, currentGameId, PLAYER_1.session.id);
		}).then(function(claimedGame){
			expect(claimedGame.error).toBe('Invalid claim.');
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	it("Should not allow claim if the user is not part of the game", function(done) {
		var currentGameId;
		var currentGame;
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		p.then(function(game){
			currentGameId = game._id;
			var moveTimeOlderThenTimeout = game.moveTime - parseInt(game.config.values['claimTimeout']);
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {moveTime: moveTimeOlderThenTimeout}}).exec();
		}).then(function(){
			return apiRunner.claimVictory(PLAYER_3.handle, currentGameId, PLAYER_3.session.id);
		}).then(function(claimedGame){
			expect(claimedGame.error).toBe('Invalid claim.');
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	it("Should not allow claim if only one user is present", function(done) {
		var currentGameId;
		var currentGame;
		
		var p = apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1, PLAYER_1.session.id);
		p.then(function(game){
			currentGameId = game._id;
			var moveTimeOlderThenTimeout = game.moveTime - parseInt(game.config.values['claimTimeout']);
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {moveTime: moveTimeOlderThenTimeout}}).exec();
		}).then(function(){
			return apiRunner.claimVictory(PLAYER_1.handle, currentGameId, PLAYER_1.session.id);
		}).then(function(claimedGame){
			expect(claimedGame.error).toBe('Invalid claim.');
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
});

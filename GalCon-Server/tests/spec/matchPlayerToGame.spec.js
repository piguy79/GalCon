var apiRunner = require('../fixtures/apiRunner'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	mongoose = require('mongoose'),
	elementBuilder = require('../fixtures/elementBuilder');

describe("Player Matching", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1, {xp : 5000});
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 5, {xp : 4000});
	
	var date = new Date();
	var threeDaysAgo = date - 1000 * 60 * 60 * 24 * 3;
	threeDaysAgo = new Date(threeDaysAgo + (1000 * 60));
	var PLAYER_3_HANDLE = "TEST_PLAYER_3";
	var PLAYER_3 = elementBuilder.createUser(PLAYER_3_HANDLE, 3, {xp : 100, expireDate : threeDaysAgo});
	
	var PLAYER_4_HANDLE = "TEST_PLAYER_4";
	var PLAYER_4 = elementBuilder.createUser(PLAYER_4_HANDLE, 7, {xp : 300, expireDate : threeDaysAgo});
	
	
	var fourDaysAgo = date - 1000 * 60 * 60 * 24 * 4;
	fourDaysAgo = new Date(fourDaysAgo);
	var MULL_HANDLE = "mull";
	var MULL = elementBuilder.createUser(MULL_HANDLE, 7, {xp : 300, expireDate : fourDaysAgo});
	
	var MAP_KEY_1 = -100;
	var MAP_KEY_2 = -200;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);
	var MAP_2 = elementBuilder.createMap(MAP_KEY_2, 5, 6);
	
	beforeEach(function(done) {
		var p = userManager.UserModel.withPromise(userManager.UserModel.create, [PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4, MULL]);
		p.then(function(){
			return mapManager.MapModel.withPromise(mapManager.MapModel.create, [MAP_1, MAP_2]);
		}).then(function(){
			done();
		});		
	});
	
	afterEach(function(done) {
		gameManager.GameModel.remove().where("map").in([MAP_KEY_1, MAP_KEY_2]).exec(function(err) {
			if(err) { console.log(err); }
			mapManager.MapModel.remove().where("key").in([MAP_KEY_1, MAP_KEY_2]).exec(function(err) {
				if(err) { console.log(err); }
				userManager.UserModel.remove().where("handle").in([PLAYER_1_HANDLE, PLAYER_2_HANDLE, PLAYER_3_HANDLE, PLAYER_4_HANDLE, MULL_HANDLE]).exec(function(err) {
					if(err) { console.log(err); }
					done();
				});
			});
		});
	});
	
	it("Should invite a random person to a game when creating one", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		p.then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1, PLAYER_1.session.id);
		}).then(function(game) {
			expect(game.map).toEqual(MAP_KEY_1);
			expect(game.players.length).toEqual(1);
			expect(game.social.invitee).toBe(PLAYER_2_HANDLE);
			expect(game.players[0].handle).toEqual(PLAYER_1_HANDLE);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
	
	it("Should look for people not within the same rank range as a fall back", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		// this also tests that players with a rank below 2 will never be picked up as PLAYER_3 should match all other conditions
		
		p.then(function(){
			return userManager.UserModel.remove({handle : PLAYER_2_HANDLE}).exec();
		}).then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1, PLAYER_1.session.id);
		}).then(function(game) {
			expect(game.map).toEqual(MAP_KEY_1);
			expect(game.players.length).toEqual(1);
			expect(game.social.invitee).toBe(PLAYER_4_HANDLE);
			expect(game.players[0].handle).toEqual(PLAYER_1_HANDLE);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
	
	it("Should default back to mull", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		p.then(function(){
			return userManager.UserModel.remove({handle : {$in : [PLAYER_2_HANDLE, PLAYER_4_HANDLE]}}).exec();
		}).then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1, PLAYER_1.session.id);
		}).then(function(game) {
			expect(game.map).toEqual(MAP_KEY_1);
			expect(game.players.length).toEqual(1);
			expect(game.social.invitee).toBe(MULL_HANDLE);
			expect(game.players[0].handle).toEqual(PLAYER_1_HANDLE);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
	
	
});
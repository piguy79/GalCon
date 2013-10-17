var needle = require("needle"),
	apiRunner = require('../fixtures/apiRunner'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	mongoose = require('mongoose'),
	elementBuilder = require('../fixtures/elementBuilder');

describe("Player Matching", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1_NAME = "TEST_PLAYER_1@gmail.xxx";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, PLAYER_1_NAME, 1);
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2_NAME = "TEST_PLAYER_2@gmail.xxx";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, PLAYER_2_NAME, 5);
	
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
	
	it("Create new game when no games are available", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		p.then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1);
		}).then(function(game) {
			expect(game.map).toEqual(MAP_KEY_1);
			expect(game.players.length).toEqual(1);
			expect(game.players[0].handle).toEqual(PLAYER_1_HANDLE);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
	
	it("Create new game when no games are available for the chosen map", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		p.then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1);
		}).then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_2_HANDLE, MAP_KEY_2);
		}).then(function(game) {
			expect(game.map).toEqual(MAP_KEY_2);
			expect(game.players.length).toEqual(1);
			expect(game.players[0].handle).toEqual(PLAYER_2_HANDLE);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
	
	it("Join game when only one other game is available for the chosen map", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		p.then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1);
		}).then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_2_HANDLE, MAP_KEY_1);
		}).then(function(game) {
			expect(game.map).toEqual(MAP_KEY_1);
			expect(game.players.length).toEqual(2);
			expect(game.players[0].handle).toEqual(PLAYER_1_HANDLE);
			expect(game.players[1].handle).toEqual(PLAYER_2_HANDLE);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
	
	it("When 2 new games are available, join the game that has the player with the closest rank", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		p.then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1);
		}).then(function(game) {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1);
		}).then(function(game) {
			return gameManager.GameModel.findOneAndUpdate({id: game.id}, {rankOfIntialPlayer: 15}).exec();
		}).then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_2_HANDLE, MAP_KEY_1);
		}).then(function(game) {
			expect(game.map).toEqual(MAP_KEY_1);
			expect(game.rankOfInitialPlayer).toEqual(1);
			expect(game.players.length).toEqual(2);
			expect(game.players[0].handle).toEqual(PLAYER_1_HANDLE);
			expect(game.players[1].handle).toEqual(PLAYER_2_HANDLE);
		}).then(done);
	});
	
	it("When 2 games are available with 1 being a new game and the other being an old game, join the old game", function(done) {
		var p = new mongoose.Promise();
		p.complete();
		
		p.then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1);
		}).then(function(game) {
			return gameManager.GameModel.findOneAndUpdate({id: game.id}, {rankOfInitialPlayer: 15, createdTime: 100}).exec();
		}).then(function(game) {
			return apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1);
		}).then(function() {
			return apiRunner.matchPlayerToGame(PLAYER_2_HANDLE, MAP_KEY_1);
		}).then(function(game) {
			expect(game.map).toEqual(MAP_KEY_1);
			expect(game.rankOfInitialPlayer).toEqual(15);
			expect(game.createdTime).toEqual(100);
			expect(game.players.length).toEqual(2);
			expect(game.players[0].handle).toEqual(PLAYER_1_HANDLE);
			expect(game.players[1].handle).toEqual(PLAYER_2_HANDLE);
		}).then(done);
	});
});
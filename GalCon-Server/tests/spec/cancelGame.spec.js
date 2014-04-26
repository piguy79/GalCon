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
	PLAYER_1.coins = 5;
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 1);
	
	var MAP_KEY_1 = -100;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);
	

	
	beforeEach(function(done) {
		(new userManager.UserModel(PLAYER_1)).save(function(err, user) {
			if(err) { console.log(err); }
			(new userManager.UserModel(PLAYER_2)).save(function(err, user) {
				if(err) { console.log(err); }
				(new mapManager.MapModel(MAP_1)).save(function(err, map) {
					if(err) { console.log(err); }
					done();
				});
			});
		});
		
		this.addMatchers({
			toBeOwnedBy : function(expected) {
				return this.actual.handle == expected;
			},
			toHaveShipNumber : function(expected) {
				return this.actual.numberOfShips == expected;
			},
			toContainMove : function(expected) {
				var gameMoves = this.actual;
				var returnVal = false;
				gameMoves.forEach(function(move) {
					if (elementMatcher.moveEquals(move, expected)) {
						returnVal = true;
					}
				});

				return returnVal;
			}
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

	it("Should allow a user to cancel when they are the only player", function(done) {
		var currentGameId;
		var p =  apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1, PLAYER_1.session.id);
		p.then(function(game) {
			currentGameId = game._id;
			return userManager.findUserByHandle(PLAYER_1_HANDLE);
		}).then(function(user){
			expect(user.coins).toBe(4);
		}).then(function() {
			var longTimeAgo = 100;
			return gameManager.GameModel.findOneAndUpdate({_id : currentGameId}, {createdDate : longTimeAgo}).exec();
		}).then(function() {
			return apiRunner.cancelGame(PLAYER_1_HANDLE, currentGameId, PLAYER_1.session.id);
		}).then(function(result){
			expect(result.success).toBe(true);
			return userManager.findUserByHandle(PLAYER_1_HANDLE);
		}).then(function(user){
			expect(user.coins).toBe(5);
			return gameManager.GameModel.findOne({_id : currentGameId}).exec();
		}).then(function(game){
			expect(game).toBe(null);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			done();
		});
	});
	
	it("Should not allow the user to cancel when more than one player is present", function(done) {
		var currentGameId;
		var p =   gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		p.then(function(game){
			currentGameId = game._id;
			return userManager.findUserByHandle(PLAYER_1_HANDLE);
		}).then(function(user){
			expect(user.coins).toBe(4);
		}).then(function() {
			var longTimeAgo = 100;
			return gameManager.GameModel.findOneAndUpdate({_id : currentGameId}, {createdDate : longTimeAgo}).exec();
		}).then(function() {
			return apiRunner.cancelGame(PLAYER_1_HANDLE, currentGameId, PLAYER_1.session.id);
		}).then(function(result){
			expect(result.success).toBe(false);
			return userManager.findUserByHandle(PLAYER_1_HANDLE);
		}).then(function(user){
			expect(user.coins).toBe(4);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			done();
		});
	});
	
	it("Should not allow the user to cancel when the game has just been created", function(done) {
		var currentGameId;
		var p =   gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		p.then(function(game){
			currentGameId = game._id;
			return userManager.findUserByHandle(PLAYER_1_HANDLE);
		}).then(function(user){
			expect(user.coins).toBe(4);
		}).then(function() {
			return apiRunner.cancelGame(PLAYER_1_HANDLE, currentGameId, PLAYER_1.session.id);
		}).then(function(result) {
			expect(result.error).toBe("User tried to cancel game before time allowance");
			done();
		}).then(null, function(err){
			expect(err).toBe(undefined);
			done();
		});
	});
});

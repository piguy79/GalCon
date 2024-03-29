
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

describe("Map xp testing -", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1, {xp : 5});
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 1, {xp : 5});
	
	var ABILITY_MAP_KEY = -100;
	var ABILITY_MAP = elementBuilder.createMap(ABILITY_MAP_KEY, 5, 6, ['attackIncrease'], 100);
	
	
	beforeEach(function(done) {
		var p = userManager.UserModel.withPromise(userManager.UserModel.create, [PLAYER_1, PLAYER_2]);
		p.then(function(){
			return mapManager.MapModel.withPromise(mapManager.MapModel.create, [ABILITY_MAP]);
		}).then(function(){
			done();
		});		
	});
	
	afterEach(function(done) {
		gameManager.GameModel.remove().where("map").in([ABILITY_MAP_KEY]).exec(function(err) {
			if(err) { console.log(err); }
			mapManager.MapModel.remove().where("key").in([ABILITY_MAP_KEY]).exec(function(err) {
				if(err) { console.log(err); }
				userManager.UserModel.remove().where("handle").in([PLAYER_1_HANDLE, PLAYER_2_HANDLE]).exec(function(err) {
					if(err) { console.log(err); }
					done();
				});
			});
		});
	});
	
	
	
	it("Should not be able to join a game you do not have xp for.", function(done){
		var currentGameId;
		var p =  apiRunner.invite(PLAYER_1_HANDLE, PLAYER_2_HANDLE, ABILITY_MAP_KEY, PLAYER_1.session.id);
		p.then(function(game){
			expect(game.error).toBe('User does not have enough xp to play this map.');
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	
});

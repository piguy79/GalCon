
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

describe("Harvest an ability planet -", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 2);
	
	var MAP_KEY_1 = -100;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);
	
	var PLAYER_1_HOME_PLANET = "HOME_PLANET_1";
	var HOME_PLANET_2 = "HOME_PLANET_2";
	var ABILITY_PLANET = "ABILITY_PLANET"
	var PLANETS = [ elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
                    elementBuilder.createPlanet(HOME_PLANET_2, PLAYER_2_HANDLE, 3, 20, { x : 3, y : 5}),
                    elementBuilder.createPlanet(ABILITY_PLANET, PLAYER_1_HANDLE, 2, 10, { x : 5, y : 2})];
	

	beforeEach(function(done) {
		var p = userManager.UserModel.withPromise(userManager.UserModel.create, [PLAYER_1, PLAYER_2]);
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
	
	
	
	it("Should setup the planet for Harvest", function(done){
		var currentGameId;
		var timeOfMove = 34728;
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1_HANDLE, PLAYER_2_HANDLE, MAP_KEY_1);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, time : timeOfMove, harvest : [{planet : ABILITY_PLANET}]}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			var abilityPlanet = _.find(game.planets, function(planet){ return planet.name === ABILITY_PLANET});
			expect(abilityPlanet.harvest.status).toBe(true);
			expect(abilityPlanet.harvest.startingRound).toBe(0);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});

	
});

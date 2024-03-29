
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
	PLAYER_1.xp = 3600;
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 2);
	PLAYER_2.xp = 3600;
	
	var ABILITY_MAP_KEY = -100;
	var ABILITY_MAP = elementBuilder.createMap(ABILITY_MAP_KEY, 5, 6, ['mixedAbility']);
	
	
	var PLAYER_1_HOME_PLANET = "HOME_PLANET_1";
	var PLAYER_2_HOME_PLANET = "PLAYER_2_HOME_PLANET";
	var ABILITY_PLANET = "ABILITY_PLANET";
	var PLANETS = [ elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
                    elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 3, 20, { x : 3, y : 5}),
                    elementBuilder.createPlanet(ABILITY_PLANET, PLAYER_1_HANDLE, 2, 10, { x : 5, y : 2}, "speedModifier")];
	

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
	
	
	
	it("Should setup the planet for Harvest", function(done){
		var currentGameId;
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, ABILITY_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, harvest : [{planet : ABILITY_PLANET}]}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			var abilityPlanet = _.find(game.planets, function(planet){ return planet.name === ABILITY_PLANET});
			expect(abilityPlanet.harvest.status).toBe("ACTIVE");
			expect(abilityPlanet.harvest.startingRound).toBe(0);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	it("Should boost my attack ability when harvesting a planet.", function(done){
		var currentGameId;
		var planets = [elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
	                    elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 0, 11, { x : 3, y : 5}),
	                    elementBuilder.createPlanet(ABILITY_PLANET, PLAYER_1_HANDLE, 2, 10, { x : 5, y : 2}, "attackModifier")];
		
		var moves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, PLAYER_2_HOME_PLANET, 8, 1) ];
		
		var p = gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, ABILITY_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: planets}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, harvest : [{planet : ABILITY_PLANET}]}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : moves, handle : PLAYER_1_HANDLE}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			var conqueredPlanet = _.find(game.planets, function(planet){ return planet.name === PLAYER_2_HOME_PLANET});
			expect(conqueredPlanet.handle).toBe(PLAYER_1_HANDLE);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	it("Should reset the harvest state on a planet when it is captured during harvest", function(done){
		var currentGameId;
		var captureHarvestPlanet = [ elementBuilder.createMove(PLAYER_2_HANDLE, PLAYER_2_HOME_PLANET, ABILITY_PLANET, 20, 1) ];
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, ABILITY_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, harvest : [{planet : ABILITY_PLANET}]}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE}, {moves : captureHarvestPlanet, handle : PLAYER_2_HANDLE});
		}).then(function(game){
			var abilityPlanet = _.find(game.planets, function(planet){ return planet.name === ABILITY_PLANET});
			expect(abilityPlanet.handle).toBe(PLAYER_2_HANDLE);
			expect(abilityPlanet.harvest.status).toBe("INACTIVE");
			expect(abilityPlanet.harvest.saveRound).toBe(1);
			// Sending 30 ships. Ability planet had 10 ships with 2 regen. One round later the num ships would be 12. 30 - 12 = 18.
			// 18 plus the bonus of 5 for capturing the planet which was about to die due to harvest makes it 23
			expect(abilityPlanet.ships).toBe(13);
			done();
		}).then(null, function(err){
			console.log(err);
			expect(true).toBe(false);
			done();
		});
	});
	
	// Note that currently we have a regen by default of 1 for planets
	it("Should further boost my defense during harvest", function(done){
		var currentGameId;
		var planets = [elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
	                    elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 0, 9, { x : 3, y : 5}),
	                    elementBuilder.createPlanet(ABILITY_PLANET, PLAYER_2_HANDLE, 2, 10, { x : 5, y : 2}, "defenseModifier")];
		
		var moves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, PLAYER_2_HOME_PLANET, 14, 1) ];
		
		var p = gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, ABILITY_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: planets}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE}, {moves : [], handle : PLAYER_2_HANDLE, harvest : [{planet : ABILITY_PLANET}]});
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : moves, handle : PLAYER_1_HANDLE}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			var defendedPlanet = _.find(game.planets, function(planet){ return planet.name === PLAYER_2_HOME_PLANET});
			expect(defendedPlanet.handle).toBe(PLAYER_2_HANDLE);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	
	it("Should give me a further speed boost during harvest", function(done){
		var currentGameId;
		var planets = [elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
	                    elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 0, 9, { x : 3, y : 5}),
	                    elementBuilder.createPlanet(ABILITY_PLANET, PLAYER_1_HANDLE, 2, 10, { x : 5, y : 2}, "speedModifier")];
		
		var moves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, PLAYER_2_HOME_PLANET, 14, 5) ];
		
		var p = gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, ABILITY_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: planets}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, harvest : [{planet : ABILITY_PLANET}]}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : moves, handle : PLAYER_1_HANDLE}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			expect(game.moves[0].duration).toBe(3.25);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	
	it("Should remove the harvest status if a saved planet is recaptured", function(done){
		var currentGameId;
		var captureHarvestPlanet = [ elementBuilder.createMove(PLAYER_2_HANDLE, PLAYER_2_HOME_PLANET, ABILITY_PLANET, 20, 1) ];
		var reCaptureHarvestPlanet = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, ABILITY_PLANET, 30, 1) ];
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, ABILITY_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, harvest : [{planet : ABILITY_PLANET}]}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			var abilityPlanet = _.find(game.planets, function(planet){ return planet.name === ABILITY_PLANET});
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE}, {moves : captureHarvestPlanet, handle : PLAYER_2_HANDLE});
		}).then(function(game){
			var abilityPlanet = _.find(game.planets, function(planet){ return planet.name === ABILITY_PLANET});
			expect(abilityPlanet.handle).toBe(PLAYER_2_HANDLE);
			expect(abilityPlanet.harvest.status).toBe("INACTIVE");
			expect(abilityPlanet.harvest.saveRound).toBe(1);
			// Sending 30 ships. Ability planet had 10 ships with 2 regen. One round later the num ships would be 12. 20 - 12 = 8.
			// 18 plus the bonus of 5 for capturing the planet which was about to die due to harvest makes it 13
			expect(abilityPlanet.ships).toBe(13);
			return gameRunner.performTurn(currentGameId, {moves : reCaptureHarvestPlanet, handle : PLAYER_1_HANDLE}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			var abilityPlanet = _.find(game.planets, function(planet){ return planet.name === ABILITY_PLANET});
			expect(abilityPlanet.handle).toBe(PLAYER_1_HANDLE);
			expect(abilityPlanet.harvest).toBe(undefined);
			done();
		}).then(null, function(err){
			console.log(err);
			expect(true).toBe(false);
			done();
		});
	});
	
	it("Should boost my ability by a percentage for each harvest planet I hold.", function(done){
		var currentGameId;
		var ABILITY_PLANET_2 = "ABILITY_PLANET_2"
		var planets = [elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
	                    elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 0, 10, { x : 3, y : 5}),
	                    elementBuilder.createPlanet(ABILITY_PLANET, PLAYER_1_HANDLE, 2, 10, { x : 5, y : 2}, "attackModifier"),
	                    elementBuilder.createPlanet(ABILITY_PLANET_2, PLAYER_1_HANDLE, 2, 10, { x : 5, y : 2}, "attackModifier")];
		
		var moves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, PLAYER_2_HOME_PLANET, 8, 1) ];
		
		var p = gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, ABILITY_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: planets}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, harvest : [{planet : ABILITY_PLANET}, {planet : ABILITY_PLANET_2}]}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : moves, handle : PLAYER_1_HANDLE}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			var conqueredPlanet = _.find(game.planets, function(planet){ return planet.name === PLAYER_2_HOME_PLANET});
			expect(conqueredPlanet.handle).toBe(PLAYER_1_HANDLE);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
});

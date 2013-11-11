
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
	
	var ATTACK_MAP_KEY = -100;
	var ATTACK_MAP = elementBuilder.createMap(ATTACK_MAP_KEY, 5, 6, ['attackIncrease']);
	
	var DEFENCE_MAP_KEY = -101;
	var DEFENCE_MAP = elementBuilder.createMap(DEFENCE_MAP_KEY, 5, 6, ['defenceIncrease']);
	
	var SPEED_MAP_KEY = -102;
	var SPEED_MAP = elementBuilder.createMap(SPEED_MAP_KEY, 5, 6, ['speedIncrease']);
	
	
	var PLAYER_1_HOME_PLANET = "HOME_PLANET_1";
	var PLAYER_2_HOME_PLANET = "PLAYER_2_HOME_PLANET";
	var ABILITY_PLANET = "ABILITY_PLANET"
	var PLANETS = [ elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
                    elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 3, 20, { x : 3, y : 5}),
                    elementBuilder.createPlanet(ABILITY_PLANET, PLAYER_1_HANDLE, 2, 10, { x : 5, y : 2}, "SPEED")];
	

	beforeEach(function(done) {
		var p = userManager.UserModel.withPromise(userManager.UserModel.create, [PLAYER_1, PLAYER_2]);
		p.then(function(){
			return mapManager.MapModel.withPromise(mapManager.MapModel.create, [ATTACK_MAP, DEFENCE_MAP, SPEED_MAP]);
		}).then(function(){
			done();
		});		
	});
	
	afterEach(function(done) {
		gameManager.GameModel.remove().where("map").in([ATTACK_MAP_KEY, DEFENCE_MAP_KEY, SPEED_MAP_KEY]).exec(function(err) {
			if(err) { console.log(err); }
			mapManager.MapModel.remove().where("key").in([ATTACK_MAP_KEY, DEFENCE_MAP_KEY, SPEED_MAP_KEY]).exec(function(err) {
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
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1_HANDLE, PLAYER_2_HANDLE, ATTACK_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, time : timeOfMove, harvest : [{planet : ABILITY_PLANET}]}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
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
		var timeOfMove = 271625;
		var planets = [elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
	                    elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 0, 11, { x : 3, y : 5}),
	                    elementBuilder.createPlanet(ABILITY_PLANET, PLAYER_1_HANDLE, 2, 10, { x : 5, y : 2}, "attackModifier")];
		
		var moves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, PLAYER_2_HOME_PLANET, 8, 1) ];
		
		var p = gameRunner.createGameForPlayers(PLAYER_1_HANDLE, PLAYER_2_HANDLE, ATTACK_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: planets}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, time : timeOfMove, harvest : [{planet : ABILITY_PLANET}]}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : moves, handle : PLAYER_1_HANDLE, time : timeOfMove}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			var conqueredPlanet = _.find(game.planets, function(planet){ return planet.name === PLAYER_2_HOME_PLANET});
			expect(conqueredPlanet.ownerHandle).toBe(PLAYER_1_HANDLE);
			expect(game.moves[0].battlestats.attackStrength).toBe(12);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	

	it("Should kill the planet once the number of rounds for enhancement have completed", function(done){
		var currentGameId;
		var timeOfMove = 271625;
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1_HANDLE, PLAYER_2_HANDLE, ATTACK_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, time : timeOfMove, harvest : [{planet : ABILITY_PLANET}]}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			var abilityPlanet = _.find(game.planets, function(planet){ return planet.name === ABILITY_PLANET});
			expect(abilityPlanet.harvest.status).toBe("ACTIVE");
			expect(abilityPlanet.harvest.startingRound).toBe(0);
		}).then(function(game){
			return gameRunner.performTurns(6, currentGameId, {moves : [], handle : PLAYER_1_HANDLE, time : timeOfMove}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			var abilityPlanet = _.find(game.planets, function(planet){ return planet.name === ABILITY_PLANET});
			expect(abilityPlanet.shipRegenRate).toBe(0);
			expect(abilityPlanet.status).toBe("DEAD");
			expect(abilityPlanet.numberOfShips).toBe(22);
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, time : timeOfMove}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			var abilityPlanet = _.find(game.planets, function(planet){ return planet.name === ABILITY_PLANET});
			expect(abilityPlanet.numberOfShips).toBe(22);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	})
	
	it("Should reset the harvest state on a planet when it is captured during harvest", function(done){
		var currentGameId;
		var timeOfMove = 271625;
		var captureHarvestPlanet = [ elementBuilder.createMove(PLAYER_2_HANDLE, PLAYER_2_HOME_PLANET, ABILITY_PLANET, 30, 1) ];
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1_HANDLE, PLAYER_2_HANDLE, ATTACK_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, time : timeOfMove, harvest : [{planet : ABILITY_PLANET}]}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, time : timeOfMove}, {moves : captureHarvestPlanet, handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			var abilityPlanet = _.find(game.planets, function(planet){ return planet.name === ABILITY_PLANET});
			expect(abilityPlanet.ownerHandle).toBe(PLAYER_2_HANDLE);
			expect(abilityPlanet.harvest.status).toBe("INACTIVE");
			expect(abilityPlanet.harvest.saveRound).toBe(1);
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
		var timeOfMove = 271625;
		var planets = [elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
	                    elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 0, 9, { x : 3, y : 5}),
	                    elementBuilder.createPlanet(ABILITY_PLANET, PLAYER_2_HANDLE, 2, 10, { x : 5, y : 2}, "defenseModifier")];
		
		var moves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, PLAYER_2_HOME_PLANET, 14, 1) ];
		
		var p = gameRunner.createGameForPlayers(PLAYER_1_HANDLE, PLAYER_2_HANDLE, DEFENCE_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: planets}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, time : timeOfMove}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove, harvest : [{planet : ABILITY_PLANET}]});
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : moves, handle : PLAYER_1_HANDLE, time : timeOfMove}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			var defendedPlanet = _.find(game.planets, function(planet){ return planet.name === PLAYER_2_HOME_PLANET});
			expect(defendedPlanet.ownerHandle).toBe(PLAYER_2_HANDLE);
			expect(game.moves[0].battlestats.defenceStrength).toBe(15);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	
	it("Should give me a further speed boost during harvest", function(done){
		var currentGameId;
		var timeOfMove = 271625;
		var planets = [elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
	                    elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 0, 9, { x : 3, y : 5}),
	                    elementBuilder.createPlanet(ABILITY_PLANET, PLAYER_1_HANDLE, 2, 10, { x : 5, y : 2}, "speedModifier")];
		
		var moves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, PLAYER_2_HOME_PLANET, 14, 5) ];
		
		var p = gameRunner.createGameForPlayers(PLAYER_1_HANDLE, PLAYER_2_HANDLE, SPEED_MAP_KEY);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: planets}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : [], handle : PLAYER_1_HANDLE, time : timeOfMove, harvest : [{planet : ABILITY_PLANET}]}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : moves, handle : PLAYER_1_HANDLE, time : timeOfMove}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			expect(game.moves[0].duration).toBe(3.25);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	it("Should give some reward to the player who saves a planet", function(done){
		done();
	});
	
	
	
});

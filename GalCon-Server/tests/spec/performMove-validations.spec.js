var mongoose = require('../../modules/model/mongooseConnection').mongoose,
	apiRunner = require('../fixtures/apiRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	elementMatcher = require('../fixtures/elementMatcher'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	_ = require('underscore');

describe("Perform Move - Validations -", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 1);
	
	var MAP_KEY_1 = -100;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);

	var PLAYER_1_HOME_PLANET = "HOME_PLANET_1";
	var UNOWNED_PLANET_1 = "UNOWNED_PLANET_1";
	var PLANETS = [ elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
	                              elementBuilder.createPlanet(UNOWNED_PLANET_1, "", 3, 20, { x : 3, y : 5}) ];
	
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

	var createMovesWithValidationSteps = function(moves, planets) {
		var currentGameId;
		var p = apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1, PLAYER_1.session.id);
		
		return p.then(function(game) {
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: planets}}).exec();
		}).then(function(game) {
			return apiRunner.performMove(currentGameId, moves, PLAYER_1_HANDLE);
		}).then(function(game) {
			expect(game.currentRound.roundNumber).toBe(0);
			return apiRunner.joinGame(currentGameId, PLAYER_2_HANDLE);
		}).then(function() {
			return apiRunner.performMove(currentGameId, [], PLAYER_2_HANDLE);
		}).then(function(game) {
			expect(game.currentRound.roundNumber).toBe(1);
			return game;
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		});
	}

	it("Moves containing the same from and to planet should be blocked", function(done) {
		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, PLAYER_1_HOME_PLANET, 6, 1);
		var moves = [ testMove ];

		var p = createMovesWithValidationSteps(moves, PLANETS);
		p.then(function(game) {
			return apiRunner.performMove(game._id, [], PLAYER_2_HANDLE);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});

});

var mongoose = require('../../modules/model/mongooseConnection').mongoose,
	apiRunner = require('../fixtures/apiRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	elementMatcher = require('../fixtures/elementMatcher'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	_ = require('underscore');

jasmine.getEnv().defaultTimeoutInterval = 30000;

describe("Perform Move - Standard -", function() {
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
		
		this.addMatchers({
			toBeOwnedBy : function(expected) {
				return this.actual.ownerHandle == expected;
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

	var createMovesWithValidationSteps = function(moves, planets) {
		var currentGameId;
		var p = apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, MAP_KEY_1);
		
		return p.then(function(game) {
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({id: game.id}, {$set: {planets: planets}}).exec();
		}).then(function(game) {
			return apiRunner.performMove(currentGameId, moves, PLAYER_1_HANDLE);
		}).then(function() {
			return apiRunner.joinGame(currentGameId, PLAYER_2_HANDLE);
		}).then(function() {
			return apiRunner.performMove(currentGameId, [], PLAYER_2_HANDLE);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		});
	}

	var notOneOfTheFollowingPlanets = function(planet, planetsToNotCheck) {
		for (var i = 0; i < planetsToNotCheck.length; i++) {
			if (planet.name = planetsToNotCheck[i]) {
				return false;
			}
		}
		return true;
	}

	it("Planet should be owned by user after move", function(done) {
		var moves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 30, 1) ];

		var p = createMovesWithValidationSteps(moves, PLANETS);
		p.then(function(game) {
			game.planets.forEach(function(planet) {
				if (planet.name == UNOWNED_PLANET_1) {
					expect(planet).toBeOwnedBy(PLAYER_1_HANDLE);
					expect(planet).toHaveShipNumber(10);
				} else if (notOneOfTheFollowingPlanets(planet, [UNOWNED_PLANET_1, PLAYER_1_HOME_PLANET ])) {
					expect(planet).not.toBeOwnedBy(PLAYER_1_HANDLE);
				}
			});
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});

	it("Planet should not be owned by user after move as not sending enough fleet", function(done) {
		var moves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 19, 1) ];

		var p = createMovesWithValidationSteps(moves, PLANETS);
		p.then(function(game) {
			game.planets.forEach(function(planet) {
				if (planet.name == UNOWNED_PLANET_1) {
					expect(planet).not.toBeOwnedBy(PLAYER_1_HANDLE);
					expect(planet).toHaveShipNumber(1);
				} else if (planet.name == PLAYER_1_HOME_PLANET) {
					expect(planet).toBeOwnedBy(PLAYER_1_HANDLE);
					expect(planet).toHaveShipNumber(14);
				}
			});
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
//
//	it("Planet should survive an attack from a smaller fleet", function(done) {
//		var moves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 6, 1) ];
//
//		createMovesWithValidationSteps(game, moves, defaultPlanetsForTest, function(dbGame) {
//			dbGame.planets.forEach(function(planet) {
//				if (planet.name == UNOWNED_PLANET_1) {
//					expect(planet).toBeOwnedBy("");
//					expect(planet).toHaveShipNumber(17);
//				}
//			});
//			done();
//		});
//	});
//
//	it("Move should be deleted after it is processed.", function(done) {
//		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 6, 1);
//		var moves = [ testMove ];
//
//		createMovesWithValidationSteps(GAME, moves, defaultPlanetsForTest, function(dbGame) {
//			expect(dbGame.moves).not.toContainMove(testMove);
//			done();
//		});
//	});
//
//	it("Should process the fleet vs numberOfShips logic before adding the regen", function(done) {
//		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 24, 1);
//		var moves = [ testMove ];
//
//		createMovesWithValidationSteps(game, moves, defaultPlanetsForTest, function(dbGame) {
//			dbGame.planets.forEach(function(planet) {
//				if (planet.name == UNOWNED_PLANET_1) {
//					expect(planet).toBeOwnedBy(PLAYER_1_HANDLE);
//					expect(planet).toHaveShipNumber(7);
//				}
//			});
//			expect(dbGame.moves).not.toContainMove(testMove);
//			done();
//		});
//	});
//
//	it("Should be able to send more ships to a friendly planet (owned by the same user)", function(done) {
//
//		var planetsForTest = [ elementBuilder.createPlanetForTest(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 10, {
//			x : 3,
//			y : 4
//		}), elementBuilder.createPlanetForTest(UNOWNED_PLANET_1, PLAYER_1_HANDLE, 0, 20, {
//			x : 3,
//			y : 5
//		}) ];
//
//		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 5, 1);
//		var moves = [ testMove ];
//
//		createMovesWithValidationSteps(game, moves, planetsForTest, function(dbGame) {
//			dbGame.planets.forEach(function(planet) {
//				if (planet.name == UNOWNED_PLANET_1) {
//					expect(planet).toBeOwnedBy(PLAYER_1_HANDLE);
//					expect(planet).toHaveShipNumber(25);
//				} else if (notOneOfTheFollowingPlanets(planet, [ UNOWNED_PLANET_1, PLAYER_1_HOME_PLANET, defaultHomePlanet.name ])) {
//					expect(planet).not.toBeOwnedBy(PLAYER_1_HANDLE);
//				}
//			});
//			expect(dbGame.moves).not.toContainMove(testMove);
//			done();
//		});
//	});
//
//	it("Round Number should be updated after perform move is called.", function(done) {
//		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 6, 1);
//		var moves = [ testMove ];
//
//		createMovesWithValidationSteps(game, moves, defaultPlanetsForTest, function(dbGame) {
//			expect(dbGame.currentRound.roundNumber).toBe(1);
//			expect(dbGame.currentRound.player).toBe(PLAYER_1_HANDLE);
//			done();
//		});
//	});
});
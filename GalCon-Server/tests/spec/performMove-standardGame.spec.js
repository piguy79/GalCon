var mongoose = require('../../modules/model/mongooseConnection').mongoose,
	apiRunner = require('../fixtures/apiRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	elementMatcher = require('../fixtures/elementMatcher'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	_ = require('underscore');

describe("Perform Move - Standard -", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 1);
	
	var MAP_KEY_1 = -100;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);

	var PLAYER_1_HOME_PLANET = "HOME_PLANET_1";
	var PLAYER_2_HOME_PLANET = "HOME_PLANET_2";
	var UNOWNED_PLANET_1 = "UNOWNED_PLANET_1";
	var PLANETS = [ elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}),
	                elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 3, 30, { x : 3, y : 4}),
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

	var notOneOfTheFollowingPlanets = function(planet, planetsToNotCheck) {
		for (var i = 0; i < planetsToNotCheck.length; i++) {
			if (planet.name = planetsToNotCheck[i]) {
				return false;
			}
		}
		return true;
	}

	it("Planet should be owned by user after move, with no regen occuring on takeover", function(done) {
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
			
			expect(game.moves.length).toBe(1);
			
			var move = game.moves[0];
			expect(move.executed).toBe(true);
			expect(move.battlestats.conquer).toBe(true);
			expect(move.battlestats.attackStrength).toBe(30);
			expect(move.battlestats.defenceStrength).toBe(20);
			expect(move.battlestats.newPlanetOwner).toBe(PLAYER_1_HANDLE);
			expect(move.battlestats.previousShipsOnPlanet).toBe(20);
			expect(move.battlestats.previousPlanetOwner).toBe('');
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});

	it("Planet should remain unowned after attack by smaller fleet", function(done) {
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
			
			expect(game.moves.length).toBe(1);
			
			var move = game.moves[0];
			expect(move.executed).toBe(true);
			expect(move.battlestats.conquer).toBe(false);
			expect(move.battlestats.attackStrength).toBe(19);
			expect(move.battlestats.defenceStrength).toBe(20);
			expect(move.battlestats.newPlanetOwner).toBe('');
			expect(move.battlestats.previousShipsOnPlanet).toBe(20);
			expect(move.battlestats.previousPlanetOwner).toBe('');
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});

	// The game is over when the second move is run
	it("Move should be deleted after next round", function(done) {
		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 6, 1);
		var moves = [ testMove ];

		var p = createMovesWithValidationSteps(moves, PLANETS);
		p.then(function(game) {
			return apiRunner.performMove(game._id, [], PLAYER_2_HANDLE);
		}).then(function(game) {
			return apiRunner.performMove(game._id, [], PLAYER_1_HANDLE);
		}).then(function(game) {
			expect(game.moves.length).toBe(0);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});

	it("Should be able to send ship reinforcements to a planet owned by the same user", function(done) {
		var ownedPlanet = "OWNED_PLANET";
		var planetsForTest = [ elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 10, {
			x : 3,
			y : 4
		}), elementBuilder.createPlanet(ownedPlanet, PLAYER_1_HANDLE, 5, 20, {
			x : 3,
			y : 5
		}) ];

		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, ownedPlanet, 5, 1);
		var moves = [ testMove ];

		var p = createMovesWithValidationSteps(moves, planetsForTest);
		p.then(function(game) {
			game.planets.forEach(function(planet) {
				if (planet.name == ownedPlanet) {
					expect(planet).toBeOwnedBy(PLAYER_1_HANDLE);
					expect(planet).toHaveShipNumber(30);
				} else if (notOneOfTheFollowingPlanets(planet, [ ownedPlanet, PLAYER_1_HOME_PLANET ])) {
					expect(planet).not.toBeOwnedBy(PLAYER_1_HANDLE);
				}
			});
			
			expect(game.moves.length).toBe(1);
			
			var move = game.moves[0];
			expect(move.executed).toBe(true);
			expect(move.battlestats.previousShipsOnPlanet).toBe(20);
			expect(move.battlestats.previousPlanetOwner).toBe(PLAYER_1_HANDLE);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
});
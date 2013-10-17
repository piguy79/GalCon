var needle = require("needle"), 
	Step = require('step'), 
	apiRunner = require('../fixtures/apiRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	elementMatcher = require('../fixtures/elementMatcher');

describe("Perform Move", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1_NAME = "TEST_PLAYER_1@gmail.xxx";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, PLAYER_1_NAME, 1);
	
	var MAP_KEY_1 = -100;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);

	var game;
	var defaultHomePlanet;
	var defaultPlanetsForTest = [ elementBuilder.createPlanetForTest("fromPlanet", "moveTest", 3, 30, { x : 3, y : 4}), 
	                              elementBuilder.createPlanetForTest("toPlanet", "", 3, 20, { x : 3, y : 5}) ];

	beforeEach(function(done) {
		var p = apiRunner.matchPlayerToGame();
		p.then(function(game) {
			game.planets.forEach(function(planet) {
				if (planet.owner == "moveTest") {
					defaultHomePlanet = planet;
				}
			});
			done();
		});

		this.addMatchers({
			toBeOwnedBy : function(expected) {
				return this.actual.owner == expected;
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

	var createMovesWithValidationSteps = function(game, moveHolder, planetsForTest, validationMethod) {
		Step(function addSomeTestPlanets() {
			apiRunner.addPlanets(game._id, planetsForTest, this);
			
			exports.addPlanetsToGame = function(gameId,planetsToAdd){
				var p = this.findById(gameId);
				return p.then(function(game) {
					planetsToAdd.forEach(function(planet){
						game.planets.push(planet);
					});
					
					return exports.saveGame(game);
				});
			}
			
			
		}, function firstMove() {
			apiRunner.performMove(game._id, moveHolder, "moveTest", this);
		}, function findGameFromDb() {
			apiRunner.findGame(game._id, this);
		}, function validateMoveExists(dbGame) {
			expect(dbGame.moves.length == 1).toBe(true);
			return true;
		}, function addSecondPlayerToGame() {
			apiRunner.joinGame(game._id, "otherPlayer", this);
		}, function secondMove(result) {
			apiRunner.performMove(game._id, [], "otherPlayer", this);
		}, function findGameFromDb() {
			apiRunner.findGame(game._id, this);
		}, function validate(dbGame) {
			validationMethod(dbGame);
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

	it("Planet should be owned by user after move.", function(done) {
		var moveHolder = [ elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet", 30, 1) ];

		createMovesWithValidationSteps(game, moveHolder, defaultPlanetsForTest, function(dbGame) {

			dbGame.planets.forEach(function(planet) {
				if (planet.name == "toPlanet") {
					expect(planet).toBeOwnedBy("moveTest");
				} else if (notOneOfTheFollowingPlanets(planet, [ "toPlanet", "fromPlanet", defaultHomePlanet.name ])) {
					expect(planet).not.toBeOwnedBy("moveTest");
				}
			});
			done();
		});

	});

	it("Planet should not be owned by user after move as not sending enough fleet.", function(done) {
		var moveHolder = [ elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet", 19, 1) ];

		createMovesWithValidationSteps(game, moveHolder, defaultPlanetsForTest, function(dbGame) {
			dbGame.planets.forEach(function(planet) {
				if (planet.name == "toPlanet") {
					expect(planet).not.toBeOwnedBy("moveTest");
					expect(planet).toHaveShipNumber(4);
				} else if (planet.name == "fromPlanet") {
					expect(planet).toBeOwnedBy("moveTest");
					expect(planet).toHaveShipNumber(14);
				}
			});
			done();
		});

	});

	it("Planet should survive an attack from a smaller fleet", function(done) {
		var moveHolder = [ elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet", 6, 1) ];

		createMovesWithValidationSteps(game, moveHolder, defaultPlanetsForTest, function(dbGame) {
			dbGame.planets.forEach(function(planet) {
				if (planet.name == "toPlanet") {
					expect(planet).toBeOwnedBy("");
					expect(planet).toHaveShipNumber(17);
				}
			});
			done();
		});

	});

	it("Move should be deleted after it is processed.", function(done) {
		var testMove = elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet", 6, 1);
		var moveHolder = [ testMove ];

		createMovesWithValidationSteps(game, moveHolder, defaultPlanetsForTest, function(dbGame) {
			expect(dbGame.moves).not.toContainMove(testMove);
			done();
		});

	});

	it("Should process the fleet vs numberOfShips logic before adding the regen", function(done) {
		var testMove = elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet", 24, 1);
		var moveHolder = [ testMove ];

		createMovesWithValidationSteps(game, moveHolder, defaultPlanetsForTest, function(dbGame) {
			dbGame.planets.forEach(function(planet) {
				if (planet.name == "toPlanet") {
					expect(planet).toBeOwnedBy("moveTest");
					expect(planet).toHaveShipNumber(7);
				}
			});
			expect(dbGame.moves).not.toContainMove(testMove);
			done();
		});

	});

	it("Should be able to send more ships to a friendly planet (owned by the same user)", function(done) {

		var planetsForTest = [ elementBuilder.createPlanetForTest("fromPlanet", "moveTest", 3, 10, {
			x : 3,
			y : 4
		}), elementBuilder.createPlanetForTest("toPlanet", "moveTest", 0, 20, {
			x : 3,
			y : 5
		}) ];

		var testMove = elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet", 5, 1);
		var moveHolder = [ testMove ];

		createMovesWithValidationSteps(game, moveHolder, planetsForTest, function(dbGame) {
			dbGame.planets.forEach(function(planet) {
				if (planet.name == "toPlanet") {
					expect(planet).toBeOwnedBy("moveTest");
					expect(planet).toHaveShipNumber(25);
				} else if (notOneOfTheFollowingPlanets(planet, [ "toPlanet", "fromPlanet", defaultHomePlanet.name ])) {
					expect(planet).not.toBeOwnedBy("moveTest");
				}
			});
			expect(dbGame.moves).not.toContainMove(testMove);
			done();
		});

	});

	it("Round Number should be updated after perform move is called.", function(done) {
		var testMove = elementBuilder.createMoveForTest("moveTest", "fromPlanet", "toPlanet", 6, 1);
		var moveHolder = [ testMove ];

		createMovesWithValidationSteps(game, moveHolder, defaultPlanetsForTest, function(dbGame) {
			expect(dbGame.currentRound.roundNumber).toBe(1);
			expect(dbGame.currentRound.player).toBe("moveTest");
			done();
		});

	});

	afterEach(function(done) {
		apiRunner.deleteGame(game._id, function() {
			done();
		});
	});

});
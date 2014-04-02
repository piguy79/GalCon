var mongoose = require('../../modules/model/mongooseConnection').mongoose,
	apiRunner = require('../fixtures/apiRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	elementMatcher = require('../fixtures/elementMatcher'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	_ = require('underscore');

describe("Perform Move - Air Attacks -", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 1);
	
	var MAP_KEY_1 = -100;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);
	
	var ATTACK_MAP_KEY_1 = -101;
	var ATTACK_MAP = elementBuilder.createMap(ATTACK_MAP_KEY_1, 5, 6, ['attackIncrease']);

	var PLAYER_1_HOME_PLANET = "HOME_PLANET_1";
	var PLAYER_2_HOME_PLANET = "HOME_PLANET_2";
	var UNOWNED_PLANET_1 = "UNOWNED_PLANET_1";
	var ABILITY_PLANET = "ABILITY_PLANET";
	var PLANETS = [ elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}),
	                elementBuilder.createPlanet(PLAYER_2_HOME_PLANET, PLAYER_2_HANDLE, 3, 30, { x : 4, y : 5}),
	                elementBuilder.createPlanet(UNOWNED_PLANET_1, "", 3, 20, { x : 3, y : 5}) ];
	
	beforeEach(function(done) {
		(new userManager.UserModel(PLAYER_1)).save(function(err, user) {
			if(err) { console.log(err); }
			(new userManager.UserModel(PLAYER_2)).save(function(err, user) {
				if(err) { console.log(err); }
				(new mapManager.MapModel(MAP_1)).save(function(err, map) {
					if(err) { console.log(err); }
					(new mapManager.MapModel(ATTACK_MAP)).save(function(err, map) {
						if(err) { console.log(err); }
						done();
					});
				});
			});
		});
		
		this.addMatchers({
			toBeOwnedBy : function(expected) {
				return this.actual.ownerHandle == expected;
			},
			toHaveShipNumber : function(expected) {
				return this.actual.ships == expected;
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
		gameManager.GameModel.remove().where("map").in([MAP_KEY_1, ATTACK_MAP_KEY_1]).exec(function(err) {
			if(err) { console.log(err); }
			mapManager.MapModel.remove().where("key").in([MAP_KEY_1, ATTACK_MAP_KEY_1]).exec(function(err) {
				if(err) { console.log(err); }
				userManager.UserModel.remove().where("handle").in([PLAYER_1_HANDLE, PLAYER_2_HANDLE]).exec(function(err) {
					if(err) { console.log(err); }
					done();
				});
			});
		});
	});

	var createMovesWithValidationSteps = function(player1Moves, player2Moves, planets, attack) {
		var currentGameId;
		
		var map = MAP_KEY_1;
		if(attack) {
			map = ATTACK_MAP_KEY_1;
		}
		
		var p = apiRunner.matchPlayerToGame(PLAYER_1_HANDLE, map, PLAYER_1.session.id);
		
		return p.then(function(game) {
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: planets}}).exec();
		}).then(function(game) {
			return apiRunner.performMove(currentGameId, player1Moves, PLAYER_1_HANDLE);
		}).then(function(game) {
			expect(game.round.num).toBe(0);
			return apiRunner.joinGame(currentGameId, PLAYER_2_HANDLE);
		}).then(function() {
			return apiRunner.performMove(currentGameId, player2Moves, PLAYER_2_HANDLE);
		}).then(function(game) {
			expect(game.round.num).toBe(1);
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
	
	var validateAirAttack = function(move, died, startFleet, remainingFleet) {
		expect(move.executed).toBe(true);
		expect(move.fleet).toBe(remainingFleet);
		expect(move.bs.diaa).toBe(died);
		expect(move.bs.startFleet).toBe(startFleet);
	}

	it("Opposing moves equal in size should destroy each other", function(done) {
		var player1Moves = [elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 10, 1)];
		var player2Moves = [elementBuilder.createMove(PLAYER_2_HANDLE, PLAYER_2_HOME_PLANET, UNOWNED_PLANET_1, 10, 1)];

		var p = createMovesWithValidationSteps(player1Moves, player2Moves, PLANETS);
		p.then(function(game) {
			game.planets.forEach(function(planet) {
				if (planet.name == UNOWNED_PLANET_1) {
					expect(planet).toBeOwnedBy("");
					expect(planet).toHaveShipNumber(20);
				}
			});
			
			expect(game.moves.length).toBe(2);
			validateAirAttack(game.moves[0], true, 10, 0);
			validateAirAttack(game.moves[1], true, 10, 0);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});

	it("Opposing moves equal in size but split into many moves should destroy each other", function(done) {
		var player1Moves = [elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 9, 1)];
		var player2Moves = [elementBuilder.createMove(PLAYER_2_HANDLE, PLAYER_2_HOME_PLANET, UNOWNED_PLANET_1, 3, 1),
		                    elementBuilder.createMove(PLAYER_2_HANDLE, PLAYER_2_HOME_PLANET, UNOWNED_PLANET_1, 3, 1),
		                    elementBuilder.createMove(PLAYER_2_HANDLE, PLAYER_2_HOME_PLANET, UNOWNED_PLANET_1, 3, 1)];

		var p = createMovesWithValidationSteps(player1Moves, player2Moves, PLANETS);
		p.then(function(game) {
			game.planets.forEach(function(planet) {
				if (planet.name == UNOWNED_PLANET_1) {
					expect(planet).toBeOwnedBy("");
					expect(planet).toHaveShipNumber(20);
				}
			});
			
			expect(game.moves.length).toBe(4);
			
			var player1Moves = _.filter(game.moves, function(move) {
				return move.playerHandle === PLAYER_1_HANDLE;
			});
			
			var player2Moves = _.filter(game.moves, function(move) {
				return move.playerHandle === PLAYER_2_HANDLE;
			});
			
			validateAirAttack(player1Moves[0], true, 9, 0);
			validateAirAttack(player2Moves[0], true, 3, 0);
			validateAirAttack(player2Moves[1], true, 3, 0);
			validateAirAttack(player2Moves[2], true, 3, 0);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
	
	it("After air attack, victor should attack planet", function(done) {
		var player1Moves = [elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 9, 1)];
		var player2Moves = [elementBuilder.createMove(PLAYER_2_HANDLE, PLAYER_2_HOME_PLANET, UNOWNED_PLANET_1, 3, 1)];

		var p = createMovesWithValidationSteps(player1Moves, player2Moves, PLANETS);
		p.then(function(game) {
			game.planets.forEach(function(planet) {
				if (planet.name == UNOWNED_PLANET_1) {
					expect(planet).toBeOwnedBy("");
					expect(planet).toHaveShipNumber(14);
				}
			});
			
			expect(game.moves.length).toBe(2);
			
			var player1Moves = _.filter(game.moves, function(move) {
				return move.playerHandle === PLAYER_1_HANDLE;
			});
			
			var player2Moves = _.filter(game.moves, function(move) {
				return move.playerHandle === PLAYER_2_HANDLE;
			});
			
			validateAirAttack(player1Moves[0], false, 9, 6);
			validateAirAttack(player2Moves[0], true, 3, 0);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
	
	it("After air attack, both victors should attack planet", function(done) {
		var player1Moves = [elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 9, 1),
		                    elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 9, 1)];
		var player2Moves = [elementBuilder.createMove(PLAYER_2_HANDLE, PLAYER_2_HOME_PLANET, UNOWNED_PLANET_1, 3, 1)];

		var p = createMovesWithValidationSteps(player1Moves, player2Moves, PLANETS);
		p.then(function(game) {
			game.planets.forEach(function(planet) {
				if (planet.name == UNOWNED_PLANET_1) {
					expect(planet).toBeOwnedBy("");
					expect(planet).toHaveShipNumber(5);
				}
			});
			
			expect(game.moves.length).toBe(3);
			
			var player1Moves = _.filter(game.moves, function(move) {
				return move.playerHandle === PLAYER_1_HANDLE;
			});
			
			var player2Moves = _.filter(game.moves, function(move) {
				return move.playerHandle === PLAYER_2_HANDLE;
			});
			
			validateAirAttack(player1Moves[0], false, 9, 6);
			validateAirAttack(player1Moves[1], false, 9, 9);
			validateAirAttack(player2Moves[0], true, 3, 0);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
	
	it("Air attack should take attack bonus into account", function(done) {
		var player1Moves = [elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 16, 1)];
		var player2Moves = [elementBuilder.createMove(PLAYER_2_HANDLE, PLAYER_2_HOME_PLANET, UNOWNED_PLANET_1, 3, 1)];

		PLANETS.push(elementBuilder.createPlanet(ABILITY_PLANET, PLAYER_1_HANDLE, 2, 10, { x : 5, y : 2}, "attackModifier"));
		var p = createMovesWithValidationSteps(player1Moves, player2Moves, PLANETS, true);
		p.then(function(game) {
			game.planets.forEach(function(planet) {
				if (planet.name == UNOWNED_PLANET_1) {
					expect(planet).toBeOwnedBy("");
					expect(planet).toHaveShipNumber(3);
				}
			});
			
			expect(game.moves.length).toBe(2);
			
			var player1Moves = _.filter(game.moves, function(move) {
				return move.playerHandle === PLAYER_1_HANDLE;
			});
			
			var player2Moves = _.filter(game.moves, function(move) {
				return move.playerHandle === PLAYER_2_HANDLE;
			});
			
			validateAirAttack(player1Moves[0], false, 16, 13);
			validateAirAttack(player2Moves[0], true, 3, 0);
		}).then(null, function(err) {
			expect(err.toString()).toBe(null);
		}).then(done);
	});
});
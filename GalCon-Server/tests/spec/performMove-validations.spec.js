var mongoose = require('../../modules/model/mongooseConnection').mongoose,
	apiRunner = require('../fixtures/apiRunner'), 
	gameRunner = require('../fixtures/gameRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	elementMatcher = require('../fixtures/elementMatcher'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	gameRunner = require('../fixtures/gameRunner'),
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

	var createMovesWithValidationSteps = function(moves, planets, handle) {
		var currentGameId;
		var p = gameRunner.createGameAwaitingAccept(PLAYER_1, PLAYER_2, MAP_KEY_1);
		
		return p.then(function(game) {
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: planets}}).exec();
		}).then(function(game) {
			return apiRunner.performMove(currentGameId, moves, handle);
		});
	}

	it("Moves containing the same from and to planet should be blocked", function(done) {
		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, PLAYER_1_HOME_PLANET, 6, 1);
		var moves = [ testMove ];

		var p = createMovesWithValidationSteps(moves, PLANETS, PLAYER_1_HANDLE);
		p.then(function(response){
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid move");
		},function(err) {
			console.log(err);
		}).then(done);
	});
	
	it("Moves with a different player handle to the user executing should be blocked", function(done) {
		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 6, 1);
		var moves = [ testMove ];

		var p = createMovesWithValidationSteps(moves, PLANETS, "FAKE");
		p.then(function(response){
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid move");
		},function(err) {
			console.log(err);
		}).then(done);
	});
	
	it("Moves need to use a valid int for fleet count", function(done) {
		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, "this", 1);
		var moves = [ testMove ];

		var p = createMovesWithValidationSteps(moves, PLANETS, PLAYER_1_HANDLE);
		p.then(function(response){
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid move");
		},function(err) {
			console.log(err);
		}).then(done);
	});
	
	it("Moves need to use a valid positive int for fleet count", function(done) {
		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, -4, 1);
		var moves = [ testMove ];

		var p = createMovesWithValidationSteps(moves, PLANETS, PLAYER_1_HANDLE);
		p.then(function(response){
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid move");
		},function(err) {
			console.log(err);
		}).then(done);
	});
	
	/////////////////////
	
	it("Move must be from a planet owned by the player", function(done) {
		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, UNOWNED_PLANET_1 ,PLAYER_1_HOME_PLANET, 10, 1);
		var moves = [ testMove ];

		var p = createMovesWithValidationSteps(moves, PLANETS, PLAYER_1_HANDLE);
		p.then(function(response){
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid move");
		},function(err) {
			console.log(err);
		}).then(done);
	});
	
	it("Moves must contain a number of ships lower then that of ships on a planet", function(done) {
		// This planet has 30 ships.
		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 31, 1);
		var moves = [ testMove ];
	
		var p = createMovesWithValidationSteps(moves, PLANETS, PLAYER_1_HANDLE);
		p.then(function(response){
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid move");
		},function(err) {
			console.log(err);
		}).then(done);
	});
	
	it("Moves must contain a number of ships lower then that of ships on a planet - In aggragate", function(done) {
		// This planet has 30 ships.
		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 10, 1);
		var testMoveExtra = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 21, 1);

		var moves = [ testMove, testMoveExtra ];
	
		var p = createMovesWithValidationSteps(moves, PLANETS, PLAYER_1_HANDLE);
		p.then(function(response){
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid move");
		},function(err) {
			console.log(err);
		}).then(done);
	});
	
	it("Moves can only be toPlanets which exist on the game board", function(done) {
		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, "FAKE", 10, 1);

		var moves = [ testMove ];
	
		var p = createMovesWithValidationSteps(moves, PLANETS, PLAYER_1_HANDLE);
		p.then(function(response){
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid move");
		},function(err) {
			console.log(err);
		}).then(done);
	});
	
	it("Moves can only be fromPlanets which exist on the game board", function(done) {
		var testMove = elementBuilder.createMove(PLAYER_1_HANDLE, "FAKE", UNOWNED_PLANET_1, 10, 1);

		var moves = [ testMove ];
	
		var p = createMovesWithValidationSteps(moves, PLANETS, PLAYER_1_HANDLE);
		p.then(function(response){
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid move");
		},function(err) {
			console.log(err);
		}).then(done);
	});
	
	it("Must only allow moves once per round", function(done) {
		var currentGameId;
		var timeOfMove = 34728;
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			return apiRunner.performMove(currentGameId, [], PLAYER_1_HANDLE);
		}).then(function(game){
			return apiRunner.performMove(currentGameId, [], PLAYER_1_HANDLE);
		}).then(function(response){
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid move");
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});

	});
	
	// Must have not moved already in this round

});

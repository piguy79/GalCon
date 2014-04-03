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

describe("Perform Move - Standard -", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	PLAYER_1.xp = 95;
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 1);
	
	var MAP_KEY_1 = -100;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);

	var PLAYER_1_HOME_PLANET = "HOME_PLANET_1";
	var PLAYER_2_HOME_PLANET = "HOME_PLANET_2";
	var UNOWNED_PLANET_1 = "UNOWNED_PLANET_1";
	var PLANETS = [ elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 50, { x : 3, y : 4}),
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

	it("Must update winners rank on winning move", function(done) {
		var currentGameId;
		var timeOfMove = 34728;
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			var player1WinningMove = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, PLAYER_2_HOME_PLANET, 50, 1) ];
			return gameRunner.performTurn(currentGameId, {moves : player1WinningMove, handle : PLAYER_1_HANDLE}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			expect(game.endGameInformation.winnerHandle).toBe(PLAYER_1_HANDLE);
			var player1 = _.filter(game.players, function(player){
				return player.handle === PLAYER_1_HANDLE;
			});
			var expectedResult = 95 + parseInt(game.config.values["xpForWinning"]) + parseInt(game.config.values["xpForPlanetCapture"]);
			expect(player1[0].xp).toBe(expectedResult);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});

	});
	
	it("Should update xp for planet capture", function(done) {
		var currentGameId;
		var timeOfMove = 34728;
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			var player1CaptureMove = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, UNOWNED_PLANET_1, 50, 1) ];
			return gameRunner.performTurn(currentGameId, {moves : player1CaptureMove, handle : PLAYER_1_HANDLE}, {moves : [], handle : PLAYER_2_HANDLE});
		}).then(function(game){
			var player1 = _.filter(game.players, function(player){
				return player.handle === PLAYER_1_HANDLE;
			});
			var expectedResult = 95 + parseInt(game.config.values["xpForPlanetCapture"]);
			expect(player1[0].xp).toBe(expectedResult);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});

	});
	
	
	
});
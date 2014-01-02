var needle = require("needle"), 
	apiRunner = require('../fixtures/apiRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	elementMatcher = require('../fixtures/elementMatcher'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	mongoose = require('mongoose'),
	gameRunner = require('../fixtures/gameRunner');

describe("Update Used Coins to track countdown to coin refresh -", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 2);
	
	var MAP_KEY_1 = -100;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);
	
	var PLAYER_1_HOME_PLANET = "HOME_PLANET_1";
	var HOME_PLANET_2 = "HOME_PLANET_2";
	var PLANETS = [ elementBuilder.createPlanet(PLAYER_1_HOME_PLANET, PLAYER_1_HANDLE, 3, 30, { x : 3, y : 4}), 
                    elementBuilder.createPlanet(HOME_PLANET_2, PLAYER_2_HANDLE, 3, 20, { x : 3, y : 5}) ];
	

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
	

	it("Should update usedCoins to time once the game is over.", function(done) {
		var currentGameId;
		var timeOfMove = 34728;
		var moves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, HOME_PLANET_2, 30, 1) ];
		
		var p =  gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		p.then(function(game){
			currentGameId = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": currentGameId}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(currentGameId, {moves : moves, handle : PLAYER_1_HANDLE, time : timeOfMove}, {moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove});
		}).then(function(game){
			expect(game.endGameInformation.winnerHandle).toBe(PLAYER_1_HANDLE);
			return userManager.findUserByHandle(PLAYER_1_HANDLE);
		}).then(function(user){
			expect(user.usedCoins).toBe(timeOfMove);
			done();
		}, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	it("Should only update usedCoins when the users last game has been completed.", function(done){
		var gameId1, gameId2;
		var timeOfMove1 = 98767;
		var timeOfMove2 = 152423;
		var winningMoves = [ elementBuilder.createMove(PLAYER_1_HANDLE, PLAYER_1_HOME_PLANET, HOME_PLANET_2, 30, 1) ];

		
		var p = gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		p.then(function(game){
			gameId1 = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": gameId1}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			return gameRunner.createGameForPlayers(PLAYER_1, PLAYER_2, MAP_KEY_1);
		}).then(function(game){
			gameId2 = game._id;
			return gameManager.GameModel.findOneAndUpdate({"_id": gameId2}, {$set: {planets: PLANETS}}).exec();
		}).then(function(game){
			return gameRunner.performTurn(gameId1, {moves : winningMoves, handle : PLAYER_1_HANDLE, time : timeOfMove1},{moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove1});
		}).then(function(game){
			return userManager.findUserByHandle(PLAYER_1_HANDLE);
		}).then(function(user){
			expect(user.usedCoins).toBe(-1);
		}).then(function(){
			return gameRunner.performTurn(gameId2, {moves : winningMoves, handle : PLAYER_1_HANDLE, time : timeOfMove2},{moves : [], handle : PLAYER_2_HANDLE, time : timeOfMove2});
		}).then(function(){
			return userManager.findUserByHandle(PLAYER_1_HANDLE);
		}).then(function(user){
			expect(user.usedCoins).toBe(timeOfMove2);
			done();
		}, function(err){
			console.log(err);
			expect(true).toBe(false);
			done();
		});
	});
	
	it("Should update usedCoins information for a user", function(done){
		var p = apiRunner.updateUserCoinsInformation(PLAYER_1_HANDLE, 20008);
		p.then(function(user){
			expect(user.usedCoins).toBe(20008);
			done();
		}, function(err){
			console.log(err);
			expect(true).toBe(false);
			done();
		});
	});

	
});

var needle = require("needle"), 
	apiRunner = require('../fixtures/apiRunner'), 
	elementBuilder = require('../fixtures/elementbuilder'), 
	elementMatcher = require('../fixtures/elementMatcher'),
	gameManager = require('../../modules/model/game'),
	userManager = require('../../modules/model/user'),
	mapManager = require('../../modules/model/map'),
	mongoose = require('mongoose');

describe("Find Games associated with a user -", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	
	var MAP_KEY_1 = -100;
	var MAP_1 = elementBuilder.createMap(MAP_KEY_1, 5, 6);

	beforeEach(function(done) {
		var p = userManager.UserModel.withPromise(userManager.UserModel.create, [PLAYER_1]);
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
				userManager.UserModel.remove().where("handle").in([PLAYER_1_HANDLE]).exec(function(err) {
					if(err) { console.log(err); }
					done();
				});
			});
		});
	});


	
});

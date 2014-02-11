var user = require('../../modules/model/user'), 
mongoose = require('mongoose'),
_ = require('underscore'),
elementBuilder = require('../fixtures/elementbuilder'), 
gameRunner = require('../fixtures/gameRunner'),
mapManager = require('../../modules/model/map'),
gameManager = require('../../modules/model/game'),
gameQueueManager = require('../../modules/model/gameQueue'),
apiRunner = require('../fixtures/apiRunner');

describe("Testing interactions with the user model", function(){
	
	var PLAYER_1_HANDLE = "player";
	var PLAYER_2_HANDLE = "otherguy";
	
	var ATTACK_MAP_KEY = -100;
	var ATTACK_MAP = elementBuilder.createMap(ATTACK_MAP_KEY, 5, 6, ['attackIncrease']);
	
	var player = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	var otherGuy = elementBuilder.createUser(PLAYER_2_HANDLE, 2);
		
	var players = [player, otherGuy];
	
	beforeEach(function(done){
		var p = user.UserModel.withPromise(user.UserModel.create, players);
		p.then(function(){
			return mapManager.MapModel.withPromise(mapManager.MapModel.create, [ATTACK_MAP]);
		}).then(function(map){
			done()
		}).then(null, function(err){
			console.log(err);
			done();
		});
	});
	
	
	it("Create an invite", function(done){
		var currentGameId;
		
		var p = apiRunner.invitePlayer(PLAYER_1_HANDLE, PLAYER_2_HANDLE, ATTACK_MAP_KEY, player.session.id);
		p.then(function(game){
			expect(game.players.length).toBe(1);
			return gameQueueManager.GameQueueModel.findOne({invitee : PLAYER_2_HANDLE}).exec();
		}).then(function(queueItem){
			expect(queueItem.invitee).toBe(PLAYER_2_HANDLE);
			done();
		}).then(null, function(err){
			expect(true).toBe(false);
			console.log(err);
			done();
		});
	});
	
	
	afterEach(function(done){
		var p = user.UserModel.remove().where('handle').in(_.pluck(players, 'handle')).exec();
		p.then(function(){
			return mapManager.MapModel.remove().where('kay').in([ATTACK_MAP_KEY]).exec();
		}).then(function(){
			return gameQueueManager.GameQueueModel.remove().where('invitee').in(_.pluck(players, 'handle')).exec();
		}).then(function(){
			return gameManager.GameModel.remove().where('map').in([ATTACK_MAP_KEY]).exec();
		}).then(function(){
			done();
		}, function(err){
			console.log(err);
			done();
		});
	});
	
});
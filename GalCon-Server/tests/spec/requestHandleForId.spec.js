var apiRunner = require("../fixtures/apiRunner"),
	elementBuilder = require("../fixtures/elementBuilder"),
	userManager = require('../../modules/model/user');

describe("Request Handle for ID", function() {
	var PLAYER_1_HANDLE = "TEST_PLAYER_1";
	var PLAYER_1 = elementBuilder.createUser(PLAYER_1_HANDLE, 1);
	
	var PLAYER_2_HANDLE = "TEST_PLAYER_2";
	var PLAYER_2 = elementBuilder.createUser(PLAYER_2_HANDLE, 5);
	
	beforeEach(function(done) {
		delete PLAYER_1.handle;
		(new userManager.UserModel(PLAYER_1)).save(function(err, user) {
			if(err) { console.log(err); }
			(new userManager.UserModel(PLAYER_2)).save(function(err, user) {
				if(err) { console.log(err); }
				done();
			});
		});
	});
	
	afterEach(function(done) {
		userManager.UserModel.remove().where("auth.google").in([PLAYER_1.auth.google, PLAYER_2.auth.google]).exec(function(err) {
			if(err) { console.log(err); }
			done();
		});
	});
	
	it("Request handle with bogus session", function(done) {
		var p = apiRunner.requestHandleForId("BAD_SESSION", PLAYER_1.auth.google,"google", "NEW_HANDLE");
		p.then(function(response) {
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid session");
		}).then(done, done);
	});
	
	it("Request handle with session that is not associated with the user", function(done) {
		var p = apiRunner.requestHandleForId("d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c91111", PLAYER_1.auth.google, "google", "NEW_HANDLE");
		p.then(function(response) {
			expect(response.session).toBe("expired");
		}).then(done, done);
	});
	
	it("Request handle with expired session", function(done) {
		var p = userManager.UserModel.findOneAndUpdate({'auth.google' : PLAYER_1.auth.google}, {$set : {'session.expireDate' : Date.now() - 1000}}).exec();
		p.then(function(user) {
			return apiRunner.requestHandleForId(PLAYER_1.session.id, PLAYER_1.auth.google, 'google', "NEW_HANDLE");
		}).then(function(response) {
			expect(response.session).toBe("expired");
		}).then(done, done);
	});
	
	it("Request valid handle", function(done) {
		var p = apiRunner.requestHandleForId(PLAYER_1.session.id, PLAYER_1.auth.google, 'google', PLAYER_1_HANDLE);
		p.then(function(response) {
			expect(response.created).toBe(true);
			expect(response.player.handle).toBe(PLAYER_1_HANDLE);
		}).then(done, done);
	});
	
	it("Request invalid handle", function(done) {
		var p = apiRunner.requestHandleForId(PLAYER_1.session.id, PLAYER_1.auth.google, 'google', "BAD*(*(&@#_HANDLE");
		p.then(function(response) {
			expect(response.valid).toBe(false);
			expect(response.player).toBe(undefined);
		}).then(done, done);
	});
	
	it("Request valid handle that is already used by another player", function(done) {
		var p = apiRunner.requestHandleForId(PLAYER_1.session.id, PLAYER_1.auth.google, 'google' , PLAYER_2_HANDLE);
		p.then(function(response) {
			expect(response.created).toBe(false);
			expect(response.player).toBe(undefined);
		}).then(done, done);
	});
	
	
	it("Request handle when user already has a handle", function(done) {
		var p = apiRunner.requestHandleForId(PLAYER_2.session.id, PLAYER_2.auth.google, 'google', "NEW_HANDLE");
		p.then(function(response) {
			expect(response.created).toBe(false);
			expect(response.player).toBe(undefined);
		}).then(done, done);
	});
})
var apiRunner = require("../fixtures/apiRunner"),
	elementBuilder = require("../fixtures/elementBuilder"),
	userManager = require('../../modules/model/user');

describe("Request Handle for Email", function() {
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
		userManager.UserModel.remove().where("email").in([PLAYER_1.email, PLAYER_2.email]).exec(function(err) {
			if(err) { console.log(err); }
			done();
		});
	});
	
	it("Request handle with bogus session", function(done) {
		var p = apiRunner.requestHandleForEmail("BAD_SESSION", PLAYER_1.email, "NEW_HANDLE");
		p.then(function(response) {
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid session");
		}).then(done, done);
	});
	
	it("Request handle with bad email", function(done) {
		var p = apiRunner.requestHandleForEmail(PLAYER_1.session.id, "boo@", "NEW_HANDLE");
		p.then(function(response) {
			expect(response.valid).toBe(false);
			expect(response.reason).toBe("Invalid email");
		}).then(done, done);
	});
	
	it("Request handle with session that is not associated with the user", function(done) {
		var p = apiRunner.requestHandleForEmail("d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c91111", PLAYER_1.email, "NEW_HANDLE");
		p.then(function(response) {
			expect(response.session).toBe("invalid");
		}).then(done, done);
	});
	
	it("Request handle with expired session", function(done) {
		var p = userManager.UserModel.findOneAndUpdate({email : PLAYER_1.email}, {$set : {'session.expireDate' : Date.now() - 1000}}).exec();
		p.then(function(user) {
			return apiRunner.requestHandleForEmail(PLAYER_1.session.id, PLAYER_1.email, "NEW_HANDLE");
		}).then(function(response) {
			expect(response.session).toBe("expired");
		}).then(done, done);
	})
	
	it("Request valid handle", function(done) {
		var p = apiRunner.requestHandleForEmail(PLAYER_1.session.id, PLAYER_1.email, "NEW_HANDLE");
		p.then(function(response) {
			expect(response.created).toBe(true);
			expect(response.player.email).toBe(PLAYER_1.email);
			expect(response.player.handle).toBe("NEW_HANDLE");
		}).then(done, done);
	});
	
	it("Request invalid handle", function(done) {
		var p = apiRunner.requestHandleForEmail(PLAYER_1.session.id, PLAYER_1.email, "BAD*(*(&@#_HANDLE");
		p.then(function(response) {
			expect(response.valid).toBe(false);
			expect(response.player).toBe(undefined);
		}).then(done, done);
	});
	
	it("Request valid handle that is already used by another player", function(done) {
		var p = apiRunner.requestHandleForEmail(PLAYER_1.session.id, PLAYER_1.email, PLAYER_2_HANDLE);
		p.then(function(response) {
			expect(response.created).toBe(false);
			expect(response.player).toBe(undefined);
		}).then(done, done);
	});
	
	
	it("Request valid handle with unregistered email", function(done) {
		var p = apiRunner.requestHandleForEmail(PLAYER_1.session.id, "fakeemail@fake.com", "NEW_HANDLE");
		p.then(function(response) {
			expect(response.session).toBe("invalid");
		}).then(done, done);
	});
	
	it("Request handle when user already has a handle", function(done) {
		var p = apiRunner.requestHandleForEmail(PLAYER_2.session.id, PLAYER_2.email, "NEW_HANDLE");
		p.then(function(response) {
			expect(response.created).toBe(false);
			expect(response.player).toBe(undefined);
		}).then(done, done);
	});
})
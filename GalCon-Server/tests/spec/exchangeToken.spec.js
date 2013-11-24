var apiRunner = require('../fixtures/apiRunner'),
	mongoose = require('mongoose');

describe("Exchange Token", function() {
	it("Invalid auth provider", function(done) {
		var p = apiRunner.exchangeToken('bad_provider', 'fake_token');
		p.then(function(res) {
			expect(res.error).not.toBe(null);
		}, function(err) {
			expect(err).toBe(null);
		}).then(done, done);
	})
});
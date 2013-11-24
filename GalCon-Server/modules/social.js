var googleapis = require('googleapis');

var isValid = function(authProvider, token) {
	if (authProvider !== "google") {
		console.log("Invalid auth provider: " + authProvider);
		return false;
	}

	if (token == null || token.length > 500) {
		console.log("Invalid token: " + token);
		return false;
	}
	return true;
}

exports.exchangeToken = function(authProvider, token) {
	if(!isValid(authProvider, token)) {
		return false;
	}
	
	googleapis.discover('plus', 'v1').execute(function(err, client) {
		console.log("discover err: " + err);
		console.log("discover client: " + client);
		client.plus.people
			.get({userId: "me"})
			.withAuthClient({access_token: token})
			.execute(function(err, result) {
				console.log("err " + err);
				console.log("res " + result);
			});
	});
}
if (process.env.NEW_RELIC_LICENSE_KEY) {
	require('newrelic');
}

var express = require('express'), routes = require('./routes'), http = require('http'), ejs = require('ejs');

var app = module.exports = express();
var server = http.createServer(app);

// Configuration
app.configure(function() {
	app.set('views', __dirname + '/views');
	app.use(express.bodyParser());
	app.use(function(req, res, next) {
		console.log("%s %s %s", req.connection.remoteAddress, req.method, req.url);
		if(req.method == "POST" && req.body) {
			console.log("%s %s", req.connection.remoteAddress, JSON.stringify(req.body));
		}
		next();
	});
	app.set("view options", {
		layout : false
	});
	app.engine('html', ejs.__express);
	app.use(express.methodOverride());
	app.use(app.router);
	app.use(express.static(__dirname + '/public'));
	app.use('/public/img', express.static(__dirname + '/public/img'));
});

app.configure('development', function() {
	app.use(express.errorHandler({
		dumpExceptions : true,
		showStack : true
	}));
});

app.configure('production', function() {
	app.use(express.errorHandler());
});

// Routes
app.get('/', routes.index);
app.post('/matchPlayerToGame', routes.matchPlayerToGame);
app.get('/findAllMaps', routes.findAllMaps);
app.get('/findGameById', routes.findGameById);
app.get('/joinGame', routes.joinGame);
app.post('/games/:id/resign', routes.resignGame);
app.get('/findUserById', routes.findUserById);
app.post('/requestHandleForId', routes.requestHandleForId);
app.get('/findGamesWithPendingMove', routes.findGamesWithPendingMove);
app.post('/performMoves', routes.performMoves);
app.get('/findCurrentGamesByPlayerHandle', routes.findCurrentGamesByPlayerHandle);
app.post('/addFreeCoins', routes.addFreeCoins);
app.post('/addCoinsForAnOrder', routes.addCoinsForAnOrder);
app.post('/deleteConsumedOrders', routes.deleteConsumedOrders);
app.get('/rank', routes.findRankInformation);
app.get('/config', routes.findConfigByType);
app.get('/inventory', routes.findAllInventory);
app.post('/sessions/exchangeToken', routes.exchangeToken);
app.get('/search/user', routes.searchUsers);
app.get('/friends', routes.findFriends);
app.post('/gamequeue/invite', routes.inviteUserToGame);
app.get('/gamequeue/pending', routes.findPendingInvites);
app.get('/gamequeue/accept', routes.acceptInvite);
app.get('/gamequeue/decline', routes.declineInvite);
app.post('/friends/match', routes.findMatchingFriends);
app.post('/user/addProvider', routes.addProviderToUser);
app.post('/game/cancel', routes.cancelGame);
app.post('/game/claim', routes.claimGame);



var port = process.env.PORT || 3000;

server.listen(port, function() {
	console.log("Express server listening on port %d in %s mode", server.address().port, app.settings.env);
});

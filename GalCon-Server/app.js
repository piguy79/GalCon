if(process.env.NODETIME_ACCOUNT_KEY) {
  require('nodetime').profile({
    accountKey: process.env.NODETIME_ACCOUNT_KEY,
    appName: 'Galcon Server',
    debug : true
  });
}

var express = require('express')
  , routes = require('./routes')
  , ejs = require('ejs');

var app = module.exports = express.createServer();

// Configuration
app.configure(function(){
  app.set('views', __dirname + '/views');
  app.use(express.bodyParser());
  app.set("view options", {layout: false});
  app.register('html', ejs);
  app.use(express.methodOverride());
  app.use(app.router);
  app.use(express.static(__dirname + '/public'));
  app.use('/public/img', express.static(__dirname + '/public/img'));
});

app.configure('development', function(){
  app.use(express.errorHandler({ dumpExceptions: true, showStack: true }));
});

app.configure('production', function(){
  app.use(express.errorHandler());
});

// Routes
app.get('/', routes.index);
app.post('/generateGame', routes.generateGame);
app.get('/findAllGames', routes.findAllGames);
app.get('/findAllMaps', routes.findAllMaps);
app.get('/findGameById', routes.findGameById);
app.get('/joinGame', routes.joinGame);
app.post('/addPlanetsToGame', routes.addPlanetsToGame);
app.get('/findAvailableGames', routes.findAvailableGames);
app.get('/findUserByUserName', routes.findUserByUserName);
app.post('/requestHandleForUserName', routes.requestHandleForUserName);
app.get('/findGamesWithPendingMove', routes.findGamesWithPendingMove);
app.post('/performMoves', routes.performMoves);
app.get('/deleteGame', routes.deleteGame);
app.get('/findCurrentGamesByPlayerHandle', routes.findCurrentGamesByPlayerHandle);
app.post('/addCoins', routes.addCoins);
app.get('/rank', routes.findRankInformation);

var port = process.env.PORT || 3000;

app.listen(port, function(){
  console.log("Express server listening on port %d in %s mode", app.address().port, app.settings.env);
});

exports.processPossibleEndGame = function(game){
	if(game.currentRound.roundNumber == 0){
	
		var playersToPlanetTheyOwn = {};
		for(var i = 0; i < game.planets.length; i++){
			var planet = game.planets[i];
			if(planet.ownerHandle && !playersToPlanetTheyOwn[planet.ownerHandle]){
				playersToPlanetTheyOwn[planet.ownerHandle] = 1;
			}else{
				playersToPlanetTheyOwn[planet.ownerHandle]++;
			}
		}
		console.log(playersToPlanetTheyOwn);
		
		var playerWithTheMostPlanets = {name : "", count : -1};
		var draw = false;
		
		for(var i = 0; i < game.players.length; i++){
			var player = game.players[i];
			if(playersToPlanetTheyOwn[player.handle] && playersToPlanetTheyOwn[player.handle] > playerWithTheMostPlanets.count){
				playerWithTheMostPlanets.name = player.handle;
				playerWithTheMostPlanets.count = playersToPlanetTheyOwn[player.handle];
			}else if(playersToPlanetTheyOwn[player.handle] && playersToPlanetTheyOwn[player.handle] == playerWithTheMostPlanets.count){
				draw = true;
			}
		}

			
		if(draw){
			game.endGame.winnerHandle = "";
			game.endGame.winningDate = Date.now();
			game.endGame.draw = true;
			
		}else{
			game.endGame.winnerHandle = playerWithTheMostPlanets.name;
			game.endGame.winningDate = Date.now();
		}
	
	}
}

exports.processRoundInformation = function(game) {
	game.currentRound.roundNumber--;
    game.updateRegenRates();
}


exports.create = function(game, players, width, height, numberOfPlanets){
	game.currentRound = {
		roundNumber : 10,
		playerHandle : players[0].handle
	};
}

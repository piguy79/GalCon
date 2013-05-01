exports.processPossibleEndGame = function(game){
	if(game.currentRound.roundNumber == 0){
	
		var playersToPlanetTheyOwn = {};
		for(var i = 0; i < game.planets.length; i++){
			var planet = game.planets[i];
			if(planet.owner && !playersToPlanetTheyOwn[planet.owner]){
				playersToPlanetTheyOwn[planet.owner] = 1;
			}else{
				playersToPlanetTheyOwn[planet.owner]++;
			}
		}
		console.log(playersToPlanetTheyOwn);
		
		var playerWithTheMostPlanets = {name : "", count : -1};
		var draw = false;
		
		for(var i = 0; i < game.players.length; i++){
			var player = game.players[i];
			if(playersToPlanetTheyOwn[player] && playersToPlanetTheyOwn[player] > playerWithTheMostPlanets.count){
				playerWithTheMostPlanets.name = player;
				playerWithTheMostPlanets.count = playersToPlanetTheyOwn[player];
			}else if(playersToPlanetTheyOwn[player] && playersToPlanetTheyOwn[player] == playerWithTheMostPlanets.count){
				draw = true;
			}
		}

			
		if(draw){
			game.endGameInformation.winner = "";
			game.endGameInformation.winningDate = new Date();
			game.endGameInformation.draw = true;
			
		}else{
			game.endGameInformation.winner = playerWithTheMostPlanets.name;
			game.endGameInformation.winningDate = new Date();
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
		player : players[0]
	};
}

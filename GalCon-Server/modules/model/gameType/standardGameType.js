exports.processPossibleEndGame = function(game){
	if(!game.hasOnlyOnePlayer()){
		var playersWhoOwnAPlanet = [];
		for(var i = 0; i < game.planets.length; i++){
			var planet = game.planets[i];
			if(planet.ownerHandle && playersWhoOwnAPlanet.indexOf(planet.ownerHandle) < 0){
				playersWhoOwnAPlanet.push(planet.ownerHandle);
			}
		}
		
		var playersWhoHaveAMove = [];
		for(var i = 0; i < game.moves.length; i++){
			var move = game.moves[i];
			if(playersWhoHaveAMove.indexOf(move.playerHandle) < 0){
				playersWhoHaveAMove.push(move.playerHandle);
			}
		}
		
		if(playersWhoOwnAPlanet.length == 1) {
			if(playersWhoHaveAMove.length == 0 || 
					(playersWhoHaveAMove.length == 1 && playersWhoHaveAMove.indexOf(playersWhoOwnAPlanet[0]) >= 0)) {
				game.endGameInformation.winner = playersWhoOwnAPlanet[0];
				game.endGameInformation.winningDate = new Date();
			}
		}
	}
}

exports.processRoundInformation = function(game) {
	game.currentRound.roundNumber++;
    game.updateRegenRates();
}

exports.applyMovesToGame = function(game){

	var i = game.moves.length;
	while (i--) {
		var move = game.moves[i];
		move.duration--;
		if (move.duration == 0) {
			game.applyMoveToPlanets(game, move);
			game.moves.splice(i, 1)
		}
	}

}

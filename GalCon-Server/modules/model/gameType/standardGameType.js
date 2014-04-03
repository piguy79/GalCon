var _ = require('underscore');

exports.processPossibleEndGame = function(game){
	if(!game.hasOnlyOnePlayer()){
		var playersWhoOwnAPlanet = [];
		for(var i = 0; i < game.planets.length; i++){
			var planet = game.planets[i];
			if(planet.handle && playersWhoOwnAPlanet.indexOf(planet.handle) < 0){
				playersWhoOwnAPlanet.push(planet.handle);
			}
		}
		
		var playersWhoHaveAMove = [];
		for(var i = 0; i < game.moves.length; i++){
			var move = game.moves[i];
			if(playersWhoHaveAMove.indexOf(move.handle) < 0){
				playersWhoHaveAMove.push(move.handle);
			}
		}
		
		if(playersWhoOwnAPlanet.length == 1) {
			if(playersWhoHaveAMove.length == 0 || 
					(playersWhoHaveAMove.length == 1 && playersWhoHaveAMove.indexOf(playersWhoOwnAPlanet[0]) == 0)) {
				game.endGame.winnerHandle = playersWhoOwnAPlanet[0];
				game.endGame.date = Date.now();
			}
		}
	}
}

exports.processRoundInformation = function(game) {
	game.round.num++;
    game.updateRegenRates();
}

exports.applyMovesToGame = function(game, multiplierMap, durationModifier) {
	if(durationModifier === undefined) {
		durationModifier = function(move) {
			return move.duration - 1;
		}
	}
	
	var movesByPlanet = {};
	_.each(game.moves, function(move) {
		var moves = movesByPlanet[move.to];
		if(moves === undefined) {
			moves = [];
		}
		moves.push(move);
		movesByPlanet[move.to] = moves;
	});
	
	_.each(_.values(movesByPlanet), function(movesArray) {
		var executedMovesByPlayer = {};
		for(var i in movesArray) {
			var move = movesArray[i];
			move.duration = durationModifier(move);
			if (move.duration <= 0) {
				move.duration = 0;
				move.executed = true;
				move.bs.startFleet = move.fleet;
				
				var executedMoves = executedMovesByPlayer[move.handle];
				if(executedMoves === undefined) {
					executedMoves = [];
				}
				executedMoves.push(move);
				executedMovesByPlayer[move.handle] = executedMoves;
			}
		}
		
		var players = _.keys(executedMovesByPlayer);
		
		if(players.length > 1) {
			var player1Moves = executedMovesByPlayer[players[0]];
			var player2Moves = executedMovesByPlayer[players[1]];
			
			for(i in player1Moves) {
				var move1 = player1Moves[i];
				move1.bs.diaa = false;
				
				for(j in player2Moves) {
					var move2 = player2Moves[j];
					if(move2.bs.diaa) {
						continue;
					}
					
					move2.bs.diaa = false;
					
					var attackMultiplier1 = 0;
					var attackMultiplier2 = 0;
					if (multiplierMap[move1.handle]) {
						attackMultiplier1 = multiplierMap[move1.handle].attackMultiplier;
					}
					if (multiplierMap[move2.handle]) {
						attackMultiplier2 = multiplierMap[move2.handle].attackMultiplier;
					}
					
					var attackStrength1 = game.calculateAttackStrengthForMove(move1, attackMultiplier1);
					var attackStrength2 = game.calculateAttackStrengthForMove(move2, attackMultiplier2);
					var battleResult = attackStrength1 - attackStrength2;
					
					move1.bs.atckMult = attackMultiplier1;
					move2.bs.atckMult = attackMultiplier2;
					
					if(battleResult == 0) {
						move1.fleet = 0;
						move2.fleet = 0;
					} else if(battleResult < 0) {
						move1.fleet = 0;
						move2.fleet = game.reverseEffectOfMultiplier(-battleResult, attackMultiplier2);
					} else if(battleResult > 0) {
						move2.fleet = 0;
						move1.fleet = game.reverseEffectOfMultiplier(battleResult, attackMultiplier1);
					}
					
					if(move1.fleet < 1) {
						move1.fleet = 0;
						move1.bs.diaa = true;
					}
					
					if(move2.fleet < 1) {
						move2.fleet = 0;
						move2.bs.diaa = true;
					}
				}
			}
		}
		
		_.each(_.values(executedMovesByPlayer), function(executedMoves) {
			_.each(executedMoves, function(executedMove) {
				if(!executedMove.bs.diaa) {
					game.applyMoveToPlanets(game, executedMove, multiplierMap);
				}
			});
		});
	});
}

standardGameType = require('./standardGameType'),
countdownGameType = require('./countdownGameType'),
speedIncreaseGameType = require('./speedIncreaseGameType');

exports.gameTypes = {
	standardGame : {
		endGameScenario : standardGameType.processPossibleEndGame,
		roundProcesser : standardGameType.processRoundInformation,
		processMoves : standardGameType.applyMovesToGame
	},
	countdownGame : {
		endGameScenario : countdownGameType.processPossibleEndGame,
		roundProcesser : countdownGameType.processRoundInformation,
		constructGameBoard : countdownGameType.create,
		processMoves : standardGameType.applyMovesToGame
	},
	speedIncrease : {
		endGameScenario : standardGameType.processPossibleEndGame,
		roundProcesser : standardGameType.processRoundInformation,
		addPlanetAbilities : speedIncreaseGameType.addPlanetAbilities,
		processMoves : speedIncreaseGameType.applyMovesToGame
	}
};
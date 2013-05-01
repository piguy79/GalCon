standardGameType = require('./standardGameType'),
countdownGameType = require('./countdownGameType');

exports.gameTypes = {
	standardGame : {
		endGameScenario : standardGameType.processPossibleEndGame,
		roundProcesser : standardGameType.processRoundInformation,
		constructGameBoard : standardGameType.create
	},
	countdownGame : {
		endGameScenario : countdownGameType.processPossibleEndGame,
		roundProcesser : countdownGameType.processRoundInformation,
		constructGameBoard : countdownGameType.create
	}
};
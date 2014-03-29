standardGameType = require('./standardGameType'),
countdownGameType = require('./countdownGameType'),
speedIncreaseGameType = require('./speedIncreaseGameType'),
attackStrengthIncreaseGameType = require('./attackStrengthIncreaseGameType'),
defenceStrengthIncreaseGameType = require('./defenceStrengthIncreaseGameType'),
mixedAbilityGameType = require('./mixedAbilityGameType');

exports.gameTypes = {
	standardGame : {
		endGameScenario : standardGameType.processPossibleEndGame,
		roundProcesser : standardGameType.processRoundInformation,
		processMoves : standardGameType.applyMovesToGame
	},
	mixedAbility : {
		endGameScenario : standardGameType.processPossibleEndGame,
		roundProcesser : standardGameType.processRoundInformation,
		processMoves : speedIncreaseGameType.applyMovesToGame,
		addPlanetAbilities : mixedAbilityGameType.addPlanetAbilities,
		findCorrectDefenseForAPlanet : defenceStrengthIncreaseGameType.findCorrectDefenseForAPlanet,
		findCorrectFleetToAttackEnemyPlanet : attackStrengthIncreaseGameType.findCorrectFleetToAttackEnemyPlanet
	},
	speedIncrease : {
		endGameScenario : standardGameType.processPossibleEndGame,
		roundProcesser : standardGameType.processRoundInformation,
		addPlanetAbilities : speedIncreaseGameType.addPlanetAbilities,
		processMoves : speedIncreaseGameType.applyMovesToGame
	},
	attackIncrease : {
		endGameScenario : standardGameType.processPossibleEndGame,
		roundProcesser : standardGameType.processRoundInformation,
		processMoves : standardGameType.applyMovesToGame,
		addPlanetAbilities : attackStrengthIncreaseGameType.addPlanetAbilities,
		findCorrectFleetToAttackEnemyPlanet : attackStrengthIncreaseGameType.findCorrectFleetToAttackEnemyPlanet
	},
	defenceIncrease : {
		endGameScenario : standardGameType.processPossibleEndGame,
		roundProcesser : standardGameType.processRoundInformation,
		processMoves : standardGameType.applyMovesToGame,
		addPlanetAbilities : defenceStrengthIncreaseGameType.addPlanetAbilities,
		findCorrectDefenseForAPlanet : defenceStrengthIncreaseGameType.findCorrectDefenseForAPlanet
	}
};
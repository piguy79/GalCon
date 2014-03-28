var gameSchema = mongoose.Schema({
	version : "Number",
	players : [{type: mongoose.Schema.ObjectId, ref: 'User'}],
	width: "Number",
	height: "Number",
	social: {
		invitee : "String",
		status : "String"
	},
	config : {
		version : "Number",
		values : {}
	},
	endGameInformation : {
		winnerHandle : "String",
		xpAwardToWinner : "Number",
		leaderboardScoreAmount : "Number",
		winningDate : "Date",
		loserHandles : [String],
		draw : "Boolean",
		declined : "String"
	},
	createdDate : "Date",
	createdTime : "Number",
	map : "Number",
	rankOfInitialPlayer : "Number",
	gameType : "String",
	currentRound : {
		roundNumber : "Number",
		playersWhoMoved : [String]
	},
	numberOfPlanets : "Number",
	planets : [
		{
			name : "String",
			ownerHandle : "String",
			isHome : "String",
			position : {
				x : "Number",
				y : "Number"
			},
			shipRegenRate : "Number",
			numberOfShips : "Number",
			population : "Number",
			ability : "String",
			harvest : {
				status : "String",
				startingRound : "Number",
				saveRound : "Number"
			},
			status : "String"
		}
	],
	moves : [
		{
			playerHandle : "String",
			fromPlanet : "String",
			toPlanet : "String",
			fleet : "Number",
			duration : "Number",
			startingRound : "Number",
			executed : "Boolean",
			previousPosition : {
				x : "Number",
				y : "Number"
			},
			currentPosition : {
				x : "Number",
				y : "Number"
			},
			startPosition : {
				x : "Number",
				y : "Number"
			},
			endPosition : {
				x : "Number",
				y : "Number"
			},
			bs : {
				previousShipsOnPlanet : "Number",
				previousPlanetOwner : "String",
				newPlanetOwner : "String",
				defenceStrength : "Number",
				attackStrength : "Number",
				defenceMultiplier : "Number",
				diaa : "Boolean"
			}
		}
	]
});
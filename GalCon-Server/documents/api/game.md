Game
-----

A game board represents the current state of a game. This information includes players, planets and moves currently being made.


GET Game
-----

* `GET /findGameById?{id}` returns a single game.

**Response**

```json
{
	"players" : ["String"],
	"width": "Number",
	"height": "Number",
	"createdDate" : "Date",
	"currentRound" : {
		"roundNumber" : "Number",
		"player" : "Number"
	},
	"numberOfPlanets" : "Number",
	"planets" : [
		{
			"name" : "String",
			"owner" : "String",
			"position" : {
				"x" : "Number",
				"y" : "Number"
			},
			"shipRegenRate" : "Number",
			"numberOfShips" : "Number"
		}
	],
	"moves" : [
		{
			"player" : "String",
			"fromPlanet" : "String",
			"toPlanet" : "String",
			"fleet" : "Number",
			"duration" : "Number"
		}
	]
	
}

```


Generate Game
-----

* `POST /generateGame?{player}&{width}&{height}`

**Response**

```json
{
	"__v":0,
	"width":10,
	"height":15,
	"createdDate":"2013-04-01T10:27:33.782Z",
	"numberOfPlanets":10,
	"_id":"515961150203ba0000000003",
	"moves":[],
	"planets":[
		{	
			"name":"Planet: 0",
			"shipRegenRate":3,
			"numberOfShips":0,
			"_id":"515961150203ba000000000d",
			"position":{
				"x":8,
				"y":3
			}
		},
		{
			"name":"Planet: 1",
			"shipRegenRate":3,
			"numberOfShips":5,
			"_id":"515961150203ba000000000c",
			"position":{
				"x":1,
				"y":13
			}
		}
	],
	"currentRound":{
		"roundNumber":0,
		"player":0
	},
	"players":["conor"]
}

```


Perform Moves
-----

Perform moves accepts an array of `move` objects. Each object must specify the from and to planet, along with the fleet (represents the number of ships being sent) and the duration (distance between from and to planets).

* `POST /performMoves`

**Request:**

```json
{
	"moves" : [
		{
			"player" : "Conor",
			"fromPlanet" : "Planet 5",
			"toPlanet" : "Planet 1",
			"fleet" : 23,
			"duration" : 5
		}	
	],
	"player" : "conor",
	"id" : 1234
}
```

**Response:**

```json
{
	"__v":0,
	"width":10,
	"height":15,
	"createdDate":"2013-04-01T10:27:33.782Z",
	"numberOfPlanets":10,
	"_id":"515961150203ba0000000003",
	"moves":[
		{
			"player" : "Conor",
			"fromPlanet" : "Planet 5",
			"toPlanet" : "Planet 1",
			"fleet" : 23,
			"duration" : 5
		}
	],
	"planets":[
		{	
			"name":"Planet: 0",
			"shipRegenRate":3,
			"numberOfShips":0,
			"_id":"515961150203ba000000000d",
			"position":{
				"x":8,
				"y":3
			}
		},
		{
			"name":"Planet: 1",
			"shipRegenRate":3,
			"numberOfShips":5,
			"_id":"515961150203ba000000000c",
			"position":{
				"x":1,
				"y":13
			}
		}
	],
	"currentRound":{
		"roundNumber":0,
		"player":0
	},
	"players":["conor"]
}
```


Join Game
-----

* `GET /joinGame?id=123456&player=conor`

**Response:**

```json
{
	"__v":0,
	"width":10,
	"height":15,
	"createdDate":"2013-04-01T10:27:33.782Z",
	"numberOfPlanets":10,
	"_id":"515961150203ba0000000003",
	"moves":[
		{
			"player" : "Conor",
			"fromPlanet" : "Planet 5",
			"toPlanet" : "Planet 1",
			"fleet" : 23,
			"duration" : 5
		}
	],
	"planets":[
		{	
			"name":"Planet: 0",
			"shipRegenRate":3,
			"numberOfShips":0,
			"_id":"515961150203ba000000000d",
			"position":{
				"x":8,
				"y":3
			}
		},
		{
			"name":"Planet: 1",
			"shipRegenRate":3,
			"numberOfShips":5,
			"_id":"515961150203ba000000000c",
			"position":{
				"x":1,
				"y":13
			}
		}
	],
	"currentRound":{
		"roundNumber":0,
		"player":0
	},
	"players":["conor", "otherplayer"]
}
```

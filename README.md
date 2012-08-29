Server side code for supporting the development of multiplayer for a space based game.

Each Section here would be better served on a WIKI. Must move over.



### Login
passport-google provides a node JS module to login using a Google ID. This would remove responsibility from us for maintaining and verifying user login information. This could save a lot of overhead.
	

[Passport Google](https://github.com/jaredhanson/passport-google)

### Deployment
Lets try using heroku this time as it is much more widely used the Cloud Foundry.

[Heroku](http://www.heroku.com/)

We will need 3 environments:

* Local : Node Js makes local setup extremlly easy. Simply install node JS, download the code and run node app.js (FLESH THIS OUT)
* Test : A Heroku server which mirrors the PROD setup so we can test new versions of the API before pushing to production.
* PROD: A Heoku Server with MongoDB Service running to support production use of the application.

### Storage
For storage the current approach will be to use mongoDB. The only user informaiton we need to store would be the email address so we can track the leaderboard scores.

### Game State
The games current sate needs to be stored in something similar to the following format:

```json
{
	"id" : 1234,
	"createdDate" : "08/27/2012 12:00:00",
	"currentRound" : 5,
	"players" {
		"player" : {
			"emailAddress" : "conor.mullen@gmail.com"
		},
		"player" : {
			"emailAddress" : "matthew.pietal@gmail.com"
		}
	},
	"moves" {
		"move" : {
			"moveId" : 2,
			"origin" : "A", 
			"destination" : "B", 
			"numberOfships" : 4,
			"player" : "conor.mullen@gmail.com"
		},
		"move" : {
			"moveId" : 1,
			"origin" : "A", 
			"destination" : "D", 
			"numberOfships" : 12,
			"matthew.pietal@gmail.com"
		}
	},
	"layout" : {
		"world" : {
			"world" : "A", 
			"position" : 3, 
			"owner" : "none", 
			"numberOfships" : 0, 
			"shipRate" : 3
		},
		"world" : {
			"world" : "B", 
			"position" : 12, 
			"owner" : "conor.mullen@gmail.com", 
			"numberOfships" : 20, 
			"shipRate" : 10
		},
		"world" : {
			"world" : "D",
			"position" : 4, 
			"owner" : "matthew.pietal@gmail.com", 
			"numberOfships" : 0, 
			"shipRate" : 3
		}
	}

}
```

### Find a game
We will need a mechanisim to "match" two players who are looking to start a game.


### Leaderboard
My thinking is this will be derived from a "User" collection. Where for each user we will store the current numbers of Wins and loses. 

### Marketing
This section should be used to provide any ideas we might have around getting the word out that the application exists. Droid life may be willing to review the application for us. Also Romain Guy is a former Google developer who has a weekly Tweet about what games he enjoys on Android currently. He has a ton of followers on Twitter. (Train getting into NS so need to stop, must flesh this out.)
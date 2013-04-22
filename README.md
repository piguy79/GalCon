Gameplay
----
My idea is to make this a turned-based multiplayer strategy game with advancement ideas.  Below are the most up-to-date thoughts on how the game will work.

__Turn-based__
* No real-time.  Players move in succession.
* All moves are captured and executed at round end, which is when all players have moved.

__Multiplayer__
* No single player mode.
* To spread adoption as wide as possible, it needs to be as EASY as possible to invite new players and to start games with existing ones.

__Advancement__
* Should be a well thought-out advancement strategy.
* Every victory earns you XP.  You get more XP from beating higher ranked opponents.
* Get enough XP to unlock a new level, which adds a new technique/element to the game.
* When you play against another player, the game will be played at the min level of all players.
* Players can pay to unlock levels if they choose.  Maybe don't allow people to buy into the upper-most levels?
* Bonus XP for achievements

Heroku
----

* [Installation/Setup and Deployment](https://github.com/piguy79/GalCon/blob/master/GalCon-Server/documents/heroku/setup.md)

API
----

* [Game Endpoint](https://github.com/piguy79/GalCon/blob/master/GalCon-Server/documents/api/game.md)


Versions
-----

__V0.01__
* Basic Game setup.
* Ability to create a new game.
* Ability to join an existing game.
* Ability to make a move within a game.

__V0.02__
* Ranking concept introduced.
* Notification system.
* Multiple levels to play.

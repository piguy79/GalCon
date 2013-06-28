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

1.0 - Targeted Features
----

1. XP/rank system
 1. Dynamic XP based upon opponent rank
2. Levels system and selection with UI on game creation - Different galaxies are opened for each group of ranks that you reach
 1. Small world - 1-5
 2. Large world - 5-10
 3. Speed ability planet - 10-12
 4. Defense ability planet - 12-14
 5. Attack ability planet - 14-16
 6. Block regen ability planet - 16-18
 7. Asteroid belt world - 18-20
 8. Black hole (dynamic speed) world - 20-22
2. Achievements
2. Leaderboards
 1. Global ranking formula
 2. Friends rank?
3. Coins for playing
 1. XX of free coins for every day you login
 2. Special coin planets appear in 2% of games?
 3. Random daily/hourly specials on coin purchases?
4. In-app payment system for coins and ad-removal
5. Display ads
6. Social - Invite through:
 1. Facebook
 2. Google+
 3. Post to twitter
7. Admin UI

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

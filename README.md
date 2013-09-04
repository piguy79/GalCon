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
* Capturing unique planets in a game earns you xp.
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
2. Achievements (Possibly 1.1)
 1. First Blood - Play your first game
 2. Comeback King - Come back from being so far behind in a  game to win it
 3. Win x games games (Can be multiple, 5,10,30 etc)
 4. Play x games games
 5. Speed Demon - Win a game while holding a speed ability
 6. Win a game by holding ALL planets
 7. Win a game in under x amount of time
 8. David vs Goliath - Beat an opponent of a higher rank
2. Leaderboards
 1. Global ranking formula
 2. Friends rank?
 3. Social Leaderboard
3. Coins for playing
 1. XX of free coins for every day you login
 2. Special coin planets appear in 2% of games?
 3. Random daily/hourly specials on coin purchases?
4. In-app payment system for coins and ad-removal
5. Display ads to remove time spent on countdown to new coins
6. Social - Invite through:
 1. Facebook
 2. Google+
 3. Post to twitter
7. Admin UI
8. Join screen redo - Basic player matching system
9. Game screen enhancements
 1. New ship selection dialog with slider to select # of ships
 2. Move bar - Show all moves in progress, with touch to highlight move on screen
 3. Show users battle results on round end
 2. Asteroid game type
 3. Black hole game type 
10. Security
11. Heroku scaling? How many users do we expect to fit on a node.
12. Graceful error handling
13. iOS?
14. Sounds (and music?)
15. Tutorial
16. Application configuration

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


__V0.03__
* Graphics improved
* Animation for moves
* Remove second player advantage
* Display Moves in progress
* Allow update/delete of current moves

__V0.04__
* Simultaneous moves in a round
* Animation for attacks
* Drag for move buttons
* Select a move to show from and to planet
* New slider based move dialog
* Level select screen
* Matching system for new games
* Performance improvments for rendering
* Introduction of scene 2D for menu systems
* Now working out attack/defence multipliers before applying moves

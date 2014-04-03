## Round 1:
	- User 1 Sees version 1 of Gameboard, with no movements yet applied
	- User 2 sees version 1 of Gameboard with no moves applied

	* User 1 sends some moves
		- User 1 should see the moves starting and also the number of ships on his/her planet should be decremented by. Call this Gameboard version 2
		- User 2 should still see version 1 of the Gameboard with no changes. This removes the unfair advantage of going second.
	* User 2 sends some moves
		- The Gameboard can now be incremented to version 2. Version 2 will be shown to each player on next refresh.
			- In order to animate version 2, we need version 1 plus the moves executed as part of the move to version 2.



### A typical animation would be


	1. Decrement planets ship counts (Abilities like regen may need to be animated)
	2. Move ships from starting x,y to new x,y (This needs to know about ability planets. For Example speed increase should be applied)
	3. Perform battle. This needs the version 1 planet to move to the version 2 planet by applying the moves used in the conversion. (Again this needs to know about abilities, attack or defence to animate correctly.)
	
	
players ["p1", "p2"]
planets [
	{
		id : 1
		owner : "p1"
		ships : 20
	}
	{
		id : 2
		owner : "p2"
		ships : 30
	}
]
moves : []

p1 makes a move.

players ["p1", "p2"]
planets [
	{
		id : 1
		owner : "p1"
		ships : 20
	}
	{
		id : 2
		owner : "p2"
		ships : 30
	}
]
moves : [
	{
		player : "p1"
		from  : 1
		to : 2
		fleet : 10
		round : 0
		duration 3
		fromx : 45
		fromy : 50
		tox   :
		toy   :
	}
]


p1 should see gameBoard with the moves he/she made in this round applied to the planet ship numbers
p2 should still see the initial gameboard


p2 makes a move:

players ["p1", "p2"]
planets [
	{
		id : 1
		owner : "p1"
		ships : 20
	}
	{
		id : 2
		owner : "p2"
		ships : 30
	}
]
moves : [
	{
		player : "p1"
		from  : 1
		to : 2
		fleet : 10
		round : 0
		duration 3
		fromx
		fromy
		tox
		toy
	}
	{
		player : "p2"
		from : 2
		to : 1
		fleet : 5
		round : 0
		duration 2
		fromx
		fromy
		tox
		toy
	}
]


Once this is executed both players should see the following:
everytime moves are executed and the duration is decremented we should update the fromx and fromy.
example
	fromx :    fromy : 
	tox   : 0  toy   : 0 
	
	fromx : 0   fromy : 0
	tox   : 10  tox   : 10
	
	fromx : 10  fromy : 10
	tox   : 20  toy   : 20


players ["p1", "p2"]
planets [
	{
		id : 1
		owner : "p1"
		ships : 10
	}
	{
		id : 2
		owner : "p2"
		ships : 25
	}
]
moves : [
	{
		player : "p1"
		from  : 1
		to : 2
		fleet : 10
		round : 0
		duration 2
		fromx
		fromy
		tox
		toy
	}
	{
		player : "p2"
		from : 2
		to : 1
		fleet : 5
		round : 0
		duration 1
	}
]

p1 makes another move

players ["p1", "p2"]
planets [
	{
		id : 1
		owner : "p1"
		ships : 10
	}
	{
		id : 2
		owner : "p2"
		ships : 25
	}
]
moves : [
	{
		player : "p1"
		from  : 1
		to : 2
		fleet : 10
		round : 0
		duration 2
		fromx
		fromy
		tox
		toy
	}
	{
		player : "p2"
		from : 2
		to : 1
		fleet : 5
		round : 0
		duration 1
		fromx
		fromy
		tox
		toy
	}
	{
		player : "p1"
		from  : 1
		to : 2
		fleet : 5
		round : 1
		duration 3
		fromx
		fromy
		tox
		toy
	}
]

p1 should see gameboard with moves for this round applied to planet ship count
p2 should just see current gameboard


p2 makes his/her move. This will cause his move which is now at 0 to execute, this will be 
reflected on everyones gameboard. 





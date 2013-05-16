## Round 1:
	- User 1 Sees version 1 of Gameboard, with no movments yet applied
	- User 2 sees version 1 of Gameboard with no moves applied

	* User 1 sends some moves
		- User 1 should see the moves atrting and also the number of ships on his/her planet decremented. Call this Gameboard version 2
		- USer 2 should still see version 1 of the Gameboard with no changes
	* User 2 sends some moves
		- The Gameboard can now be incremented to version 2. Version 2 will be shown to each player on next refresh.
			- In order to animate version 2, we need version 1 plus the moves executed as part of the move to version 2.



### A typical animation would be


	1. Decremenet planets ship counts
	2. Move ships from starting x,y to new x,y (This needs to know about ability planets. For Example speed increase should be applied)
	3. Perform battle. This needs the version 1 planet to move to the version 2 planet by applying the moves used in the conversion. (Again this needs to know about abilities, attack or defence to animate correctly.)
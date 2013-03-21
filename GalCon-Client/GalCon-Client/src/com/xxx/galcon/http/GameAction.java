package com.xxx.galcon.http;


import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;

/**
 * This class defines a set of methods used to interact with the server side code.
 * 
 * 
 * @author conormullen
 *
 */
public interface GameAction {
	

	
	/**
	 * This method allows the current user to join a game. The only parameter needed is the username.
	 * A GameBoard object will be returned representing the current state of the Game.
	 * 
	 * 
	 * @param player
	 * @return <GameBoard> Representing current state.
	 */
	public GameBoard generateGame(String player);
	
	public AvailableGames findAllGames();
	
	public GameBoard joinGame(String id, String player);
	
}

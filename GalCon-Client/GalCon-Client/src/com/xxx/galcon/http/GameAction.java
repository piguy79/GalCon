package com.xxx.galcon.http;

import java.util.List;

import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;

/**
 * This class defines a set of methods used to interact with the server side
 * code.
 * 
 * 
 * @author conormullen
 * 
 */
public interface GameAction {

	public static final String POST = "post";
	public static final String JSON_POST = "json_post";
	public static final String GET = "get";

	/**
	 * This method allows the current user to join a game. The only parameter
	 * needed is the username. A GameBoard object will be returned representing
	 * the current state of the Game.
	 * 
	 * @return <GameBoard> Representing current state.
	 */
	public GameBoard generateGame(String player, int width, int height) throws ConnectionException;

	public AvailableGames findAvailableGames() throws ConnectionException;
	
	public GameBoard findGameById(String id) throws ConnectionException;

	public GameBoard joinGame(String id, String player) throws ConnectionException;

	public GameBoard performMoves(String gameId, List<Move> moves) throws ConnectionException;

}

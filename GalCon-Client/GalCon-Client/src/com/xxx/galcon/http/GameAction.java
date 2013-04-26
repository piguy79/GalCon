package com.xxx.galcon.http;

import java.util.List;

import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.Rank;

/**
 * This class defines a set of methods used to interact with the server side
 * code.
 * 
 * 
 * @author conormullen
 * 
 */
public interface GameAction {

	/**
	 * This method allows the current user to join a game. The only parameter
	 * needed is the username. A GameBoard object will be returned representing
	 * the current state of the Game.
	 * 
	 * @return <GameBoard> Representing current state.
	 */
	public void generateGame(ConnectionResultCallback<GameBoard> callback, String player, int width, int height)
			throws ConnectionException;

	public void findAvailableGames(ConnectionResultCallback<AvailableGames> callback, String player)
			throws ConnectionException;

	public void findGameById(ConnectionResultCallback<GameBoard> callback, String id) throws ConnectionException;

	public void joinGame(ConnectionResultCallback<GameBoard> callback, String id, String player)
			throws ConnectionException;

	public void performMoves(ConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves)
			throws ConnectionException;

	public void findActiveGamesForAUser(ConnectionResultCallback<AvailableGames> callback, String player)
			throws ConnectionException;
	
	public void findUserInformation(ConnectionResultCallback<Player> callback, String player) throws ConnectionException;

	public void findGamesWithPendingMove(ConnectionResultCallback<AvailableGames> callback, String player)
			throws ConnectionException;
	
}

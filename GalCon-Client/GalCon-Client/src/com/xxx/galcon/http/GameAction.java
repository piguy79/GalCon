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
	public void generateGame(UIConnectionResultCallback<GameBoard> callback, String player, int width, int height, String gameType)
			throws ConnectionException;

	public void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String player)
			throws ConnectionException;

	public void findGameById(UIConnectionResultCallback<GameBoard> callback, String id) throws ConnectionException;

	public void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String player)
			throws ConnectionException;

	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves)
			throws ConnectionException;

	public void findActiveGamesForAUser(UIConnectionResultCallback<AvailableGames> callback, String player)
			throws ConnectionException;
	
	public void findUserInformation(UIConnectionResultCallback<Player> callback, String player) throws ConnectionException;

	public void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String player)
			throws ConnectionException;
	
}

package com.xxx.galcon.http;

import java.util.List;

import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Player;

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
	public void generateGame(UIConnectionResultCallback<GameBoard> callback, String playerHandle, int width,
			int height, String gameType) throws ConnectionException;

	public void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String playerHandle)
			throws ConnectionException;

	public void findGameById(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle)
			throws ConnectionException;

	public void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle)
			throws ConnectionException;

	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves)
			throws ConnectionException;

	public void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback, String playerHandle)
			throws ConnectionException;

	public void findUserInformation(UIConnectionResultCallback<Player> callback, String player);

	public void requestHandleForUserName(UIConnectionResultCallback<HandleResponse> callback, String userName,
			String handle);

	public void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String playerHandle)
			throws ConnectionException;

}

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
 * @author conormullen
 */
public interface GameAction {

	public void generateGame(UIConnectionResultCallback<GameBoard> callback, String playerHandle, int width,
			int height, String gameType);

	public void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String playerHandle);

	public void findGameById(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle);

	public void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle);

	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves);

	public void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback, String playerHandle);

	public void findUserInformation(UIConnectionResultCallback<Player> callback, String player);

	public void requestHandleForUserName(UIConnectionResultCallback<HandleResponse> callback, String userName,
			String handle);

	public void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String playerHandle)
			throws ConnectionException;

}

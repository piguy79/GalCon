package com.xxx.galcon.http;

import java.util.List;

import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Player;

/**
 * This class defines a set of methods used to interact with the server side
 * code.
 * 
 * @author conormullen
 */
public interface GameAction {

	
	public void matchPlayerToGame(UIConnectionResultCallback<GameBoard> callback, String playerHandle, Long mapToFind);

	public void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String playerHandle);
	
	public void findAllMaps(UIConnectionResultCallback<Maps> callback);

	public void findGameById(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle);

	public void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle);

	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves);

	public void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback, String playerHandle);

	public void findUserInformation(UIConnectionResultCallback<Player> callback, String player);
	
	public void findConfigByType(UIConnectionResultCallback<Configuration> callback, String type);

	public void requestHandleForUserName(UIConnectionResultCallback<HandleResponse> callback, String userName,
			String handle);

	public void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String playerHandle)
			throws ConnectionException;
	
	public void addCoins(UIConnectionResultCallback<Player> callback, String playerHandle, Long numCoins, Long usedCoins) throws ConnectionException;
	
	public void reduceTimeUntilNextGame(UIConnectionResultCallback<Player> callback,String playerHandle, Long timeRemaining, Long usedCoins ) throws ConnectionException;
	
	public void showAd();

}

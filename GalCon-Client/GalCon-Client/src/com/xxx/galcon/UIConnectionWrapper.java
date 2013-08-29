package com.xxx.galcon;

import java.util.List;

import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Player;

public class UIConnectionWrapper {
	private static GameAction gameAction;

	private UIConnectionWrapper() {
		super();
	}

	public static void setGameAction(GameAction gameAction) {
		UIConnectionWrapper.gameAction = gameAction;
	}

	public static void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves) {
		gameAction.performMoves(callback, gameId, moves);
	}

	public static void findAllMaps(UIConnectionResultCallback<Maps> callback) {
		gameAction.findAllMaps(callback);
	}

	public static void findGameById(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle) {
		gameAction.findGameById(callback, id, playerHandle);
	}

	public static void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback,
			String playerHandle) {
		gameAction.findCurrentGamesByPlayerHandle(callback, playerHandle);
	}

	public static void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String player) {
		try {
			gameAction.findGamesWithPendingMove(callback, player);
		} catch (ConnectionException e) {
			callback.onConnectionError(e.getMessage());
		}
	}

	public static void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String playerHandle) {
		gameAction.findAvailableGames(callback, playerHandle);
	}

	public static void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle) {
		gameAction.joinGame(callback, id, playerHandle);

	}
	
	public static void addCoins(UIConnectionResultCallback<Player> callback, String playerHandle, Long numCoins, Long usedCoins){
		try{
			gameAction.addCoins(callback, playerHandle, numCoins, usedCoins);
		}catch (ConnectionException e){
			callback.onConnectionError(e.getMessage());
		}
	}
	
	public static void reduceTimeUntilCoins(UIConnectionResultCallback<Player> callback, String playerHandle,Long timeRemaining, Long usedCoins){
		try{
			gameAction.reduceTimeUntilNextGame(callback, playerHandle,timeRemaining ,usedCoins);
		}catch(ConnectionException e){
			callback.onConnectionError(e.getMessage());
		}
	}
	
	public static void findconfigByType(UIConnectionResultCallback<Configuration> callback, String type){
		gameAction.findConfigByType(callback, type);
	}
}

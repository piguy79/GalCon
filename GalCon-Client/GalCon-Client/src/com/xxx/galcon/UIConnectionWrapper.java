package com.xxx.galcon;

import java.util.List;

import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.People;
import com.xxx.galcon.model.Player;

public class UIConnectionWrapper {
	private static GameAction gameAction;

	private UIConnectionWrapper() {
		super();
	}

	public static void setGameAction(GameAction gameAction) {
		UIConnectionWrapper.gameAction = gameAction;
	}

	public static void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves,
			List<HarvestMove> harvestMoves) {
		gameAction.performMoves(callback, gameId, moves, harvestMoves);
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

	public static void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String playerHandle) {
		gameAction.findAvailableGames(callback, playerHandle);
	}

	public static void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle) {
		gameAction.joinGame(callback, id, playerHandle);
	}

	public static void reduceTimeUntilCoins(UIConnectionResultCallback<Player> callback, String playerHandle) {
		gameAction.reduceTimeUntilNextGame(callback, playerHandle);
	}

	public static void findconfigByType(UIConnectionResultCallback<Configuration> callback, String type) {
		gameAction.findConfigByType(callback, type);
	}
	
	public static void searchForPlayers(UIConnectionResultCallback<People> callback, String searchTerm){
		gameAction.searchForPlayers(callback, searchTerm);
	}
}

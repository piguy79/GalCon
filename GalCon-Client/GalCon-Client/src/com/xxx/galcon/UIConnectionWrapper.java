package com.xxx.galcon;

import java.util.List;

import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;

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
}

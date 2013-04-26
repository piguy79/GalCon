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
		try {
			gameAction.performMoves(callback, gameId, moves);
		} catch (ConnectionException e) {
			callback.onConnectionError(e.getMessage());
		}
	}

	public static void findGameById(UIConnectionResultCallback<GameBoard> callback, String id) {
		try {
			gameAction.findGameById(callback, id);
		} catch (ConnectionException e) {
			callback.onConnectionError(e.getMessage());
		}
	}

	public static void findActiveGamesForAUser(UIConnectionResultCallback<AvailableGames> callback, String player) {
		try {
			gameAction.findActiveGamesForAUser(callback, player);
		} catch (ConnectionException e) {
			callback.onConnectionError(e.getMessage());
		}
	}

	public static void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String player) {
		try {
			gameAction.findGamesWithPendingMove(callback, player);
		} catch (ConnectionException e) {
			callback.onConnectionError(e.getMessage());
		}
	}

	public static void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String player) {
		try {
			gameAction.findAvailableGames(callback, player);
		} catch (ConnectionException e) {
			callback.onConnectionError(e.getMessage());
		}
	}

	public static void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String player) {
		try {
			gameAction.joinGame(callback, id, player);
		} catch (ConnectionException e) {
			callback.onConnectionError(e.getMessage());
		}
	}
}

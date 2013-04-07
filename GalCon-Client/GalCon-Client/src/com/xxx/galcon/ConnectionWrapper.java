package com.xxx.galcon;

import java.util.List;

import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.ConnectionResultCallback;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;

public class ConnectionWrapper {
	private static GameAction gameAction;

	private ConnectionWrapper() {
		super();
	}

	public static void setGameAction(GameAction gameAction) {
		ConnectionWrapper.gameAction = gameAction;
	}

	public static void performMoves(ConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves) {
		try {
			gameAction.performMoves(callback, gameId, moves);
		} catch (ConnectionException e) {
			// FIXME: handle
			e.printStackTrace();
		}
	}

	public static void findGameById(ConnectionResultCallback<GameBoard> callback, String id) {
		try {
			gameAction.findGameById(callback, id);
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

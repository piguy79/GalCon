package com.xxx.galcon;

import java.util.List;

import com.xxx.galcon.http.ConnectionException;
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

	public static GameBoard performMoves(String gameId, List<Move> moves) {
		try {
			return gameAction.performMoves(gameId, moves);
		} catch (ConnectionException e) {
			// FIXME: handle
			e.printStackTrace();
		}

		return null;
	}

	public static GameBoard findGameById(String id) {
		try {
			return gameAction.findGameById(id);
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}

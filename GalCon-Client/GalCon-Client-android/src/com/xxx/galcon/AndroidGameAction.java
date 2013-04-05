package com.xxx.galcon;

import java.util.List;

import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;

public class AndroidGameAction implements GameAction {

	public AvailableGames findAvailableGames() {
		// TODO Auto-generated method stub
		return null;
	}

	public GameBoard joinGame(String id, String player) {
		// TODO Auto-generated method stub
		return null;
	}

	public GameBoard generateGame(String player, int width, int height) throws ConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	public GameBoard performMoves(String gameId, List<Move> moves) throws ConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	public GameBoard findGameById(String id) throws ConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

}

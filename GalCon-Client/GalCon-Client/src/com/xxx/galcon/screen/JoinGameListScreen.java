package com.xxx.galcon.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.model.GameBoard;

public class JoinGameListScreen extends GameListScreen {

	public JoinGameListScreen(AssetManager assetManager) {
		super(assetManager);
	}

	@Override
	public BoardScreen takeActionOnGameboard(GameAction gameAction, GameBoard toTakeActionOn, String user,
			BoardScreen boardScreen) {
		try {
			gameAction.joinGame(new SetGameBoardResultHandler(boardScreen), toTakeActionOn.id, user);
		} catch (ConnectionException e) {

			e.printStackTrace();
		}
		return boardScreen;
	}

	@Override
	protected boolean showGamesThatHaveBeenWon() {
		return false;
	}
}

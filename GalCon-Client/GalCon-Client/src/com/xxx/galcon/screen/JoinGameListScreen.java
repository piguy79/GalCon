package com.xxx.galcon.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.model.GameBoard;

public class JoinGameListScreen extends GameListScreen {

	public JoinGameListScreen(AssetManager assetManager) {
		super(assetManager);
	}

	@Override
	public BoardScreen takeActionOnGameboard(GameAction gameAction, GameBoard toTakeActionOn, String playerHandle,
			BoardScreen boardScreen) {
		UIConnectionWrapper.joinGame(new SetGameBoardResultHandler(boardScreen), toTakeActionOn.id, playerHandle);
		return boardScreen;
	}

	@Override
	protected boolean showGamesThatHaveBeenWon() {
		return false;
	}

	@Override
	protected void refreshScreen() {
		UIConnectionWrapper.findAvailableGames(this, GameLoop.USER.handle);
	}
}

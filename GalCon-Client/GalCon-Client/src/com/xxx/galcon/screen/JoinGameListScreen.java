package com.xxx.galcon.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.screen.overlay.TextOverlay;

public class JoinGameListScreen extends GameListScreen {

	public JoinGameListScreen(AssetManager assetManager) {
		super(assetManager);
	}

	@Override
	public void takeActionOnGameboard(GameBoard toTakeActionOn, String playerHandle) {
		UIConnectionWrapper.joinGame(new SelectGameResultHander(), toTakeActionOn.id, playerHandle);
	}

	@Override
	protected boolean showGamesThatHaveBeenWon() {
		return false;
	}

	@Override
	protected void refreshScreen() {
		overlay = new TextOverlay("Refreshing...", assetManager, true);
		UIConnectionWrapper.findAvailableGames(this, GameLoop.USER.handle);
	}

	@Override
	public void resetState() {
		super.resetState();
		UIConnectionWrapper.findAvailableGames(this, GameLoop.USER.handle);
	}
}

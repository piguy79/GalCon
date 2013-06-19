package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.GameBoard;

public class BoardScreenHud extends Hud {
	private GameBoard gameBoard;

	private HudButton endTurnButton;
	private HudButton sendButton;
	private HudButton refreshButton;

	public BoardScreenHud(AssetManager assetManager) {
		super();

		sendButton = new SendHudButton(assetManager);
		endTurnButton = new EndTurnHudButton(assetManager);
		refreshButton = new RefreshHudButton(assetManager);

		addHudButton(sendButton);
		addHudButton(endTurnButton);
		addHudButton(refreshButton);

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	public void associateCurrentRoundInformation(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}

	@Override
	public void render(float delta) {
		boolean isMyTurn = haveRoundInformation() && gameBoard.currentPlayerToMove.equals(GameLoop.USER.handle);

		getSpriteBatch().begin();

		refreshButton.setEnabled(true);

		if (gameBoard.wasADraw() || gameBoard.hasWinner()) {
			sendButton.setEnabled(false);
			endTurnButton.setEnabled(false);
			refreshButton.setEnabled(false);
		} else if (!isMyTurn) {
			sendButton.setEnabled(false);
			endTurnButton.setEnabled(false);
		} else {
			sendButton.setEnabled(true);
			endTurnButton.setEnabled(true);
		}

		getSpriteBatch().end();

		super.render(delta);
	}

	private boolean haveRoundInformation() {
		if (gameBoard.currentPlayerToMove != null) {
			return true;
		}
		return false;
	}
}

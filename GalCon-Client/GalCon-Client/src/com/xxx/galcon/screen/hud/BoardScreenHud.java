package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.EndGameInformation;
import com.xxx.galcon.model.GameBoard;

public class BoardScreenHud extends Hud {
	private GameBoard gameBoard;

	private HudButton backButton;
	private HudButton endTurnButton;
	private HudButton sendButton;
	private HudButton refreshButton;

	public BoardScreenHud(AssetManager assetManager) {
		super();

		sendButton = new SendHudButton(assetManager);
		backButton = new BackHudButton(assetManager);
		endTurnButton = new EndTurnHudButton(assetManager);
		refreshButton = new RefreshHudButton(assetManager);

		addHudButton(sendButton);
		addHudButton(backButton);
		addHudButton(endTurnButton);
		addHudButton(refreshButton);

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	public void associateCurrentRoundInformation(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}

	@Override
	public void render(float delta) {
		boolean isMyTurn = haveRoundInformation() && gameBoard.currentPlayerToMove.equals(GameLoop.USER.name);

		getSpriteBatch().begin();

		refreshButton.setEnabled(true);

		if (gameBoard.wasADraw() || gameBoard.hasWinner()) {
			sendButton.setEnabled(false);
			endTurnButton.setEnabled(false);
			refreshButton.setEnabled(false);
		} else if (!isMyTurn) {
			int height = Gdx.graphics.getHeight();
			BitmapFont font = Fonts.getInstance().largeFont();
			font.draw(getSpriteBatch(), "Current Player: "
					+ (gameBoard.currentPlayerToMove.isEmpty() ? "Waiting for opponent" : gameBoard.currentPlayerToMove), 5, height * .26f);
			font.draw(getSpriteBatch(), "Round Number: " + gameBoard.roundNumber, 5, height * .2f);

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

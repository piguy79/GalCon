package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.GameBoard;

public class BoardScreenHud extends Hud {
	private static final float BOTTOM_HEIGHT_RATIO = 0.13f;
	private GameBoard gameBoard;

	private HudButton endTurnButton;
	private Texture bottomBar;

	public BoardScreenHud(AssetManager assetManager) {
		super();

		endTurnButton = new EndTurnHudButton(assetManager);
		
		bottomBar = assetManager.get("data/images/bottom_bar.png", Texture.class);

		addHudButton(endTurnButton);

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	public void associateCurrentRoundInformation(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}

	@Override
	public void render(float delta) {
		boolean isMyTurn = haveRoundInformation() && gameBoard.currentPlayerToMove.equals(GameLoop.USER.handle);

		getSpriteBatch().begin();

		getSpriteBatch().draw(bottomBar, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * BOTTOM_HEIGHT_RATIO);

		if (gameBoard.wasADraw() || gameBoard.hasWinner()) {
			endTurnButton.setEnabled(false);
		} else if (!isMyTurn) {
			endTurnButton.setEnabled(false);
		} else {
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

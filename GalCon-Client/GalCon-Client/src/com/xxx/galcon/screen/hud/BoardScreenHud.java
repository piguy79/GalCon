package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;

public class BoardScreenHud extends Hud {
	private BitmapFont font;
	private String currentPlayerToMove;
	private int roundNumber;
	private String winner;

	private HudButton backButton;
	private HudButton endTurnButton;
	private HudButton sendButton;
	private HudButton refreshButton;

	public BoardScreenHud(AssetManager assetManager) {
		super();
		font = Fonts.getInstance().largeFont();

		sendButton = new SendHudButton(assetManager.get("data/images/arrow_right.png", Texture.class));
		backButton = new BackHudButton(assetManager.get("data/images/arrow_left.png", Texture.class));
		endTurnButton = new EndTurnHudButton(assetManager.get("data/images/end_turn.png", Texture.class));
		refreshButton = new RefreshHudButton(assetManager.get("data/images/refresh.png", Texture.class));

		addHudButton(sendButton);
		addHudButton(backButton);
		addHudButton(endTurnButton);
		addHudButton(refreshButton);

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	public void associateCurrentRoundInformation(String currentPlayerToMove, int roundNumber, String winner) {
		this.currentPlayerToMove = currentPlayerToMove;
		this.roundNumber = roundNumber;
		this.winner = winner;
	}

	@Override
	public void render(float delta) {
		boolean isMyTurn = haveRoundInformation() && currentPlayerToMove.equals(GameLoop.USER);

		getSpriteBatch().begin();

		refreshButton.setEnabled(true);
		if (winner != null && !winner.isEmpty()) {
			sendButton.setEnabled(false);
			endTurnButton.setEnabled(false);
			refreshButton.setEnabled(false);
		} else if (!isMyTurn) {
			int height = Gdx.graphics.getHeight();
			font.draw(getSpriteBatch(), "Current Player: " + currentPlayerToMove, 5, height * .26f);
			font.draw(getSpriteBatch(), "Round Number: " + roundNumber, 5, height * .2f);

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
		if (currentPlayerToMove != null) {
			return true;
		}
		return false;
	}
}

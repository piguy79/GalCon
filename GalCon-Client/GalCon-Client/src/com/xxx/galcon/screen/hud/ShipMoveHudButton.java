package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.screen.Action;

public class ShipMoveHudButton extends HudButton {

	private Move move;
	private boolean isPending;
	private int xPos;

	public ShipMoveHudButton(Move move, boolean isPending, int xPos, AssetManager assetManager) {
		super(assetManager.get("data/images/bottom_bar_ship_button.png", Texture.class));
		this.move = move;
		this.isPending = isPending;
		this.xPos = xPos;
	}

	@Override
	public String getActionOnClick() {
		return Action.SHIP_MOVE;
	}

	@Override
	public void updateLocationAndSize(int screenWidth, int screenHeight) {
		int bottomHeight = (int) (screenHeight * BoardScreenHud.BOTTOM_HEIGHT_RATIO);
		int maxMinimizedBarWidth = (int) (screenWidth * 0.66f);
		int buttonHeight = (int) (bottomHeight * 0.65f);

		int margin = (int) (screenWidth * 0.015f);

		this.x = margin + xPos * buttonHeight + xPos * margin;
		this.y = (int) (bottomHeight * 0.5f - buttonHeight / 2);
		this.width = buttonHeight;
		this.height = buttonHeight;

		if (this.x + this.width + margin > maxMinimizedBarWidth) {
			this.setEnabled(false);
		}
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		if (isPending) {
			spriteBatch.setColor(Color.ORANGE);
		}
		super.render(spriteBatch);
		spriteBatch.setColor(Color.WHITE);

		if (isEnabled()) {
			BitmapFont font = Fonts.getInstance().mediumFont();
			if (!isPending) {
				font.setColor(Color.BLACK);
			}
			font.draw(spriteBatch, "" + move.shipsToMove, x + 3, y + height - 10);
			font.setColor(Color.WHITE);
		}
	}
}
package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.screen.Action;

public class ShipMoveHudButton extends HudButton {
	private int margin;
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
		return Action.SHIP_MOVE + "-" + move.hashCode();
	}

	@Override
	public void updateLocationAndSize(int screenWidth, int screenHeight) {
		int bottomHeight = (int) (screenHeight * BoardScreenHud.BOTTOM_HEIGHT_RATIO);
		int buttonHeight = (int) (bottomHeight * 0.65f);

		margin = (int) (screenWidth * 0.015f);

		this.x = margin + xPos * buttonHeight + xPos * margin;
		this.y = (int) (bottomHeight * 0.5f - buttonHeight / 2);
		this.width = buttonHeight;
		this.height = buttonHeight;

		if (!isButtonInbounds()) {
			this.setEnabled(false);
		}
	}
	
	

	@Override
	public void render(SpriteBatch spriteBatch) {
		if (isPending) {
			spriteBatch.setColor(Color.ORANGE);
		} else if (move.executed) {
			spriteBatch.setColor(Color.BLUE);
		}
		super.render(spriteBatch);
		spriteBatch.setColor(Color.WHITE);

		if (isEnabled()) {
			BitmapFont font = Fonts.getInstance().largeFont();
			if (!isPending && !move.executed) {
				font.setColor(Color.RED);

			}
			font.draw(spriteBatch, "" + move.shipsToMove, x + 5, y + height - 10);

			String duration = "" + (int) Math.ceil(move.duration);
			font.setColor(Color.BLACK);

			TextBounds bounds = font.getBounds(duration);
			font.draw(spriteBatch, duration, x + width - 5 - bounds.width, y + 5 + bounds.height);
			font.setColor(Color.WHITE);
		}
	}

	public void offSet(int xDiff) {
		x = x + xDiff;

		if (isButtonInbounds()) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

	private boolean isButtonInbounds() {
		return (this.x + this.width + margin) < BoardScreenHud.MAX_BAR_WIDTH_FOR_MOVES;
	}
}

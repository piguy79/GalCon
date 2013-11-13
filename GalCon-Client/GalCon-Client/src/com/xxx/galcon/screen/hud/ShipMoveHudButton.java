package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.screen.Action;

public class ShipMoveHudButton extends Button {
	private int buttonMargin;
	private Move move;
	private boolean isPending;
	private int xPos;

	private AssetManager assetManager;

	public ShipMoveHudButton(AssetManager assetManager, Move move, boolean isPending, int xPos, TextureAtlas atlas) {
		super(atlas.findRegion("bottom_bar_ship_button"));
		this.move = move;
		this.isPending = isPending;
		this.xPos = xPos;
		this.assetManager = assetManager;
	}

	@Override
	public String getActionOnClick() {
		return Action.SHIP_MOVE + "-" + move.hashCode();
	}

	@Override
	public void updateLocationAndSize(int x, int y, int screenWidth, int screenHeight) {
		int bottomHeight = (int) (screenHeight * BoardScreenHud.BOTTOM_HEIGHT_RATIO);
		int buttonHeight = (int) (bottomHeight * 0.65f);

		buttonMargin = (int) (screenWidth * 0.015f);

		this.x = BoardScreenHud.START_X_BAR_FOR_MOVES + xPos * buttonHeight + xPos * buttonMargin;
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
			BitmapFont font = Fonts.getInstance(assetManager).largeFont();
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
		boolean rightBounds = (this.x + this.width + buttonMargin) < BoardScreenHud.MAX_BAR_WIDTH_FOR_MOVES;
		boolean leftBounds = this.x >= BoardScreenHud.START_X_BAR_FOR_MOVES;

		return rightBounds && leftBounds;
	}
}

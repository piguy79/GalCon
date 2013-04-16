package com.xxx.galcon.screen;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;

public class BoardScreenHud implements ScreenFeedback {
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private List<HudButton> hudButtons = new ArrayList<HudButton>();
	private Action returnResult = null;
	private String currentPlayerToMove;
	private int roundNumber;

	private static abstract class HudButton {
		protected static final float BUTTON_SIZE_RATIO = 0.15f;
		protected static final int MARGIN = 30;
		private boolean disabled = false;

		protected int x, y, width, height;
		private Texture texture;

		public HudButton(Texture texture) {
			super();
			this.texture = texture;
		}

		abstract public void updateLocationAndSize(int screenWidth, int screenHeight);

		abstract public Action getActionOnClick();

		abstract protected boolean disabledWhenNotMyTurn();

		public boolean isTouched(int touchX, int touchY) {
			if (disabled) {
				return false;
			}

			if (touchX >= x && touchX <= x + width) {
				if (touchY >= y && touchY <= y + height) {
					return true;
				}
			}

			return false;
		}

		public void render(SpriteBatch spriteBatch, boolean isMyTurn) {
			disabled = !isMyTurn && disabledWhenNotMyTurn();
			if (!disabled) {
				spriteBatch.draw(texture, x, y, width, height);
			}
		}
	}

	public static class SendHudButton extends HudButton {

		public SendHudButton(Texture texture) {
			super(texture);
		}

		@Override
		public Action getActionOnClick() {
			return Action.SEND;
		}

		@Override
		public void updateLocationAndSize(int screenWidth, int screenHeight) {
			int buttonWidth = (int) (screenWidth * BUTTON_SIZE_RATIO);
			this.x = MARGIN;
			this.y = MARGIN + buttonWidth + MARGIN;
			this.width = buttonWidth;
			this.height = buttonWidth;
		}

		@Override
		protected boolean disabledWhenNotMyTurn() {
			return true;
		}
	}

	public static class BackHudButton extends HudButton {

		public BackHudButton(Texture texture) {
			super(texture);
		}

		@Override
		public Action getActionOnClick() {
			return Action.BACK;
		}

		@Override
		public void updateLocationAndSize(int screenWidth, int screenHeight) {
			int buttonWidth = (int) (screenWidth * BUTTON_SIZE_RATIO);
			this.x = MARGIN;
			this.y = MARGIN;
			this.width = buttonWidth;
			this.height = buttonWidth;
		}

		@Override
		protected boolean disabledWhenNotMyTurn() {
			return false;
		}
	}

	public static class EndTurnHudButton extends HudButton {

		public EndTurnHudButton(Texture texture) {
			super(texture);
		}

		@Override
		public Action getActionOnClick() {
			return Action.END_TURN;
		}

		@Override
		public void updateLocationAndSize(int screenWidth, int screenHeight) {
			int buttonWidth = (int) (screenWidth * BUTTON_SIZE_RATIO);
			this.x = screenWidth - buttonWidth - MARGIN;
			this.y = MARGIN + buttonWidth + MARGIN + 15;
			this.width = buttonWidth;
			this.height = (int) (buttonWidth * 0.6f);
		}

		@Override
		protected boolean disabledWhenNotMyTurn() {
			return true;
		}
	}

	public static class RefreshHudButton extends HudButton {
		public RefreshHudButton(Texture texture) {
			super(texture);
		}

		@Override
		public Action getActionOnClick() {
			return Action.REFRESH;
		}

		@Override
		public void updateLocationAndSize(int screenWidth, int screenHeight) {
			int buttonWidth = (int) (screenWidth * BUTTON_SIZE_RATIO);
			this.x = screenWidth - buttonWidth - MARGIN;
			this.y = MARGIN;
			this.width = buttonWidth;
			this.height = buttonWidth;
		}

		@Override
		protected boolean disabledWhenNotMyTurn() {
			return false;
		}
	}

	public BoardScreenHud(AssetManager assetManager) {
		spriteBatch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_32.fnt"),
				Gdx.files.internal("data/fonts/tahoma_32.png"), false);
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

		hudButtons.add(new SendHudButton(assetManager.get("data/images/arrow_right.png", Texture.class)));
		hudButtons.add(new BackHudButton(assetManager.get("data/images/arrow_left.png", Texture.class)));
		hudButtons.add(new EndTurnHudButton(assetManager.get("data/images/end_turn.png", Texture.class)));
		hudButtons.add(new RefreshHudButton(assetManager.get("data/images/refresh.png", Texture.class)));

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	public void associateCurrentRoundInformation(String currentPlayerToMove, int roundNumber) {
		this.currentPlayerToMove = currentPlayerToMove;
		this.roundNumber = roundNumber;
	}

	@Override
	public void render(float delta) {
		boolean isMyTurn = haveRoundInformation() && currentPlayerToMove.equals(GameLoop.USER);
		processTouch();

		spriteBatch.begin();

		if (!isMyTurn) {
			int height = Gdx.graphics.getHeight();
			font.draw(spriteBatch, "Current Player: " + currentPlayerToMove, 5, height * .26f);
			font.draw(spriteBatch, "Round Number: " + roundNumber, 5, height * .2f);
		}

		for (int i = 0; i < hudButtons.size(); ++i) {
			hudButtons.get(i).render(spriteBatch, isMyTurn);
		}

		spriteBatch.end();
	}

	private boolean haveRoundInformation() {
		if (currentPlayerToMove != null) {
			return true;
		}
		return false;
	}

	private void processTouch() {
		returnResult = null;

		if (Gdx.input.justTouched()) {
			int x = Gdx.input.getX();
			int y = Gdx.graphics.getHeight() - Gdx.input.getY();

			for (int i = 0; i < hudButtons.size(); ++i) {
				if (hudButtons.get(i).isTouched(x, y)) {
					returnResult = hudButtons.get(i).getActionOnClick();
				}
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		for (int i = 0; i < hudButtons.size(); ++i) {
			hudButtons.get(i).updateLocationAndSize(width, height);
		}
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getRenderResult() {
		return returnResult;
	}

	@Override
	public void resetState() {
		returnResult = null;
	}

}

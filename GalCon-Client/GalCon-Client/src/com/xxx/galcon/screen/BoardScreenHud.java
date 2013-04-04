package com.xxx.galcon.screen;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.ScreenFeedback;

public class BoardScreenHud implements ScreenFeedback {
	public static final String SEND_BUTTON = "send_button";
	public static final String END_TURN_BUTTON = "end_turn_button";

	private SpriteBatch spriteBatch;
	private List<HudButton> hudButtons = new ArrayList<HudButton>();
	private String returnResult = null;

	private static abstract class HudButton {
		protected int x, y, width, height;
		private Texture texture;

		public HudButton(Texture texture) {
			super();
			this.texture = texture;
		}

		abstract public void updateLocationAndSize(int screenWidth, int screenHeight);

		abstract public String getButtonId();

		public boolean isTouched(int touchX, int touchY) {
			if (touchX >= x && touchX <= x + width) {
				if (touchY >= y && touchY <= y + height) {
					return true;
				}
			}

			return false;
		}

		public void render(SpriteBatch spriteBatch) {
			spriteBatch.draw(texture, x, y, width, height);
		}
	}

	public static class SendHudButton extends HudButton {

		public SendHudButton(Texture texture) {
			super(texture);
		}

		@Override
		public String getButtonId() {
			return SEND_BUTTON;
		}

		@Override
		public void updateLocationAndSize(int screenWidth, int screenHeight) {
			this.x = 15;
			this.y = 150;
			this.width = 80;
			this.height = 80;
		}
	}

	public static class EndTurnHudButton extends HudButton {

		public EndTurnHudButton(Texture texture) {
			super(texture);
		}

		@Override
		public String getButtonId() {
			return END_TURN_BUTTON;
		}

		@Override
		public void updateLocationAndSize(int screenWidth, int screenHeight) {
			int buttonWidth = 80;
			this.x = screenWidth - buttonWidth - 15;
			this.y = 160;
			this.width = buttonWidth;
			this.height = 50;
		}
	}

	public BoardScreenHud(AssetManager assetManager) {
		spriteBatch = new SpriteBatch();

		hudButtons.add(new SendHudButton(assetManager.get("data/images/arrow_right.png", Texture.class)));
		hudButtons.add(new EndTurnHudButton(assetManager.get("data/images/end_turn.png", Texture.class)));

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void render(float delta) {
		processTouch();

		spriteBatch.begin();

		for (int i = 0; i < hudButtons.size(); ++i) {
			hudButtons.get(i).render(spriteBatch);
		}

		spriteBatch.end();
	}

	private void processTouch() {
		returnResult = null;

		if (Gdx.input.justTouched()) {
			int x = Gdx.input.getX();
			int y = Gdx.graphics.getHeight() - Gdx.input.getY();

			for (int i = 0; i < hudButtons.size(); ++i) {
				if (hudButtons.get(i).isTouched(x, y)) {
					returnResult = hudButtons.get(i).getButtonId();
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

}

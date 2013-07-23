package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Button {
	protected static final float BUTTON_SIZE_RATIO = 0.15f;
	protected static final int MARGIN = 10;
	private boolean enabled = true;

	protected int x, y, width, height;
	private Texture texture;

	public Button(Texture texture) {
		super();
		this.texture = texture;
	}

	abstract public void updateLocationAndSize(int x, int y, int width, int height);

	abstract public String getActionOnClick();

	public boolean isTouched(int touchX, int touchY) {
		if (!enabled) {
			return false;
		}

		if (touchX >= x && touchX <= x + width) {
			if (touchY >= y && touchY <= y + height) {
				return true;
			}
		}

		return false;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void render(SpriteBatch spriteBatch) {
		if (enabled) {
			spriteBatch.draw(texture, x, y, width, height);
		}
	}

	public int getWidth() {
		return width;
	}
}
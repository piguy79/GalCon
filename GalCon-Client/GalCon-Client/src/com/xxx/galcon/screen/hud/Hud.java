package com.xxx.galcon.screen.hud;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.screen.Action;

public abstract class Hud implements ScreenFeedback {
	private SpriteBatch spriteBatch;
	private Action returnResult = null;
	private List<HudButton> hudButtons = new ArrayList<HudButton>();
	private boolean touchEnabled = true;

	public Hud() {
		spriteBatch = new SpriteBatch();
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

	public void setTouchEnabled(boolean touchEnabled) {
		this.touchEnabled = touchEnabled;
	}

	private void processTouch() {
		returnResult = null;
		if (!touchEnabled) {
			return;
		}

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

	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}

	public void addHudButton(HudButton button) {
		hudButtons.add(button);
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

package com.xxx.galcon.screen.hud;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.InGameInputProcessor.TouchPoint;

public abstract class Hud implements ScreenFeedback {
	private SpriteBatch spriteBatch;
	private String returnResult = null;
	private List<Button> hudButtons = new ArrayList<Button>();
	private boolean touchEnabled = true;

	public Hud() {
		spriteBatch = new SpriteBatch();
	}

	@Override
	public void render(float delta) {
		processTouch();

		spriteBatch.begin();

		doRender(delta, spriteBatch);

		for (int i = 0; i < hudButtons.size(); ++i) {
			hudButtons.get(i).render(spriteBatch);
		}

		spriteBatch.end();
	}

	public void doRender(float delta, SpriteBatch spriteBatch) {

	}

	public void setTouchEnabled(boolean touchEnabled) {
		this.touchEnabled = touchEnabled;
	}

	private void processTouch() {
		returnResult = null;
		if (!touchEnabled) {
			return;
		}

		InGameInputProcessor ip = (InGameInputProcessor) Gdx.input.getInputProcessor();
		if (ip.didTouch()) {
			TouchPoint touchPoint = ip.getTouch();
			int x = touchPoint.x;
			int y = Gdx.graphics.getHeight() - touchPoint.y;

			for (int i = 0; i < hudButtons.size(); ++i) {
				if (hudButtons.get(i).isTouched(x, y)) {
					ip.consumeTouch();
					returnResult = hudButtons.get(i).getActionOnClick();
				}
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		for (int i = 0; i < hudButtons.size(); ++i) {
			hudButtons.get(i).updateLocationAndSize(0, 0, width, height);
		}
	}

	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}

	public void addHudButton(Button button) {
		hudButtons.add(button);
	}

	public List<Button> getHudButtons() {
		return hudButtons;
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

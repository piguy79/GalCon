package com.xxx.galcon;

import com.badlogic.gdx.InputProcessor;

public class InGameInputProcessor implements InputProcessor {

	private static final int MAX_TOUCH_PROCESSING_DELAY_IN_MILLISECONDS = 500;
	private int lastTouchX = -1, lastTouchY = -1;
	private long touchTime = System.currentTimeMillis();

	public class TouchPoint {
		public int x;
		public int y;
	}

	private TouchPoint touchPoint = new TouchPoint();

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean didTouch() {
		if (touchTime < System.currentTimeMillis() - MAX_TOUCH_PROCESSING_DELAY_IN_MILLISECONDS) {
			consumeTouch();
			return false;
		}
		return (lastTouchX != -1 && lastTouchY != -1);
	}

	public TouchPoint getTouch() {
		touchPoint.x = lastTouchX;
		touchPoint.y = lastTouchY;

		return touchPoint;
	}

	/**
	 * Clears the touch so that no one else can consume it.
	 */
	public void consumeTouch() {
		lastTouchX = -1;
		lastTouchY = -1;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		return true;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		touchTime = System.currentTimeMillis();
		lastTouchX = x;
		lastTouchY = y;
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

}

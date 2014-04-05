package com.railwaygames.solarsmash;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;

public class InGameInputProcessor implements InputProcessor {

	private static final int MAX_TOUCH_PROCESSING_DELAY_IN_MILLISECONDS = 500;
	private int lastTouchX = -1, lastTouchY = -1;
	private long touchTime = System.currentTimeMillis();

	private int lastKeyCode = -1;
	private long lastKeyCodeTime = System.currentTimeMillis();

	private boolean dragDetected = false;
	private TouchPoint dragBeginPoint = null;
	private TouchPoint dragDiffBeginPoint = null;
	private TouchPoint dragDiffEndPoint = null;

	public class TouchPoint {
		public int x;
		public int y;

		public TouchPoint() {
			x = 0;
			y = 0;
		}

		public TouchPoint(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	private TouchPoint touchPoint = new TouchPoint();

	@Override
	public boolean keyDown(int keycode) {
		lastKeyCodeTime = System.currentTimeMillis();
		lastKeyCode = keycode;

		return false;
	}

	public boolean isBackKeyPressed() {
		return lastKeyCode == Keys.BACK
				&& lastKeyCodeTime > System.currentTimeMillis() - MAX_TOUCH_PROCESSING_DELAY_IN_MILLISECONDS;
	}

	public void consumeKey() {
		lastKeyCode = -1;
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
		touchPoint.y = Gdx.graphics.getHeight() - lastTouchY;

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
		if (!isDragging()) {
			touchTime = System.currentTimeMillis();
			lastTouchX = x;
			lastTouchY = y;
		}
		dragBeginPoint = null;
		dragDiffEndPoint = null;
		dragDiffBeginPoint = null;
		dragDetected = false;
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		y = Gdx.graphics.getHeight() - y;

		TouchPoint tp = new TouchPoint(x, y);
		if (dragBeginPoint == null) {
			dragBeginPoint = tp;
		}

		if (dragDiffBeginPoint == null) {
			dragDiffBeginPoint = tp;
		}

		dragDiffEndPoint = tp;

		return true;
	}

	public boolean isDragging() {
		if (dragDetected) {
			return true;
		}
		if (dragBeginPoint == null || dragDiffEndPoint == null) {
			return false;
		}
		if (Math.abs(dragDiffEndPoint.x - dragBeginPoint.x) > (float) Gdx.graphics.getWidth() * .01f) {
			dragDetected = true;
		}

		return dragDetected;
	}

	public TouchPoint getDragBeginPoint() {
		return dragBeginPoint;
	}

	public int getDragXDiff(boolean consumeDiff) {
		if (dragDiffEndPoint == null || dragDiffBeginPoint == null) {
			return 0;
		}
		int xDiff = dragDiffEndPoint.x - dragDiffBeginPoint.x;

		if (consumeDiff) {
			dragDiffBeginPoint = dragDiffEndPoint;
			dragDiffEndPoint = null;
		}

		return xDiff;
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

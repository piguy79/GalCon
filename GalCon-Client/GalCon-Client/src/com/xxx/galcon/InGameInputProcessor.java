package com.xxx.galcon;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

public class InGameInputProcessor implements InputProcessor {

	private static final int MAX_TOUCH_PROCESSING_DELAY_IN_MILLISECONDS = 500;
	private int lastTouchX = -1, lastTouchY = -1;
	private long touchTime = System.currentTimeMillis();
	private List<TouchPoint> draggedPoints = new ArrayList<TouchPoint>(50);

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
		if (!isDragging()) {
			touchTime = System.currentTimeMillis();
			lastTouchX = x;
			lastTouchY = y;
		}
		draggedPoints.clear();
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		y = Gdx.graphics.getHeight() - y;
		draggedPoints.add(new TouchPoint(x, y));

		return true;
	}

	public boolean isDragging() {
		return draggedPoints.size() > 8;
	}

	public List<TouchPoint> getDragTouchPoints() {
		return draggedPoints;
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

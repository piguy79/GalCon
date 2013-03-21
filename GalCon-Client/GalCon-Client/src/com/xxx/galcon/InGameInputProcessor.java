package com.xxx.galcon;

import com.badlogic.gdx.InputProcessor;

public class InGameInputProcessor implements InputProcessor {

	public int lastTouchX = -1, lastTouchY = -1;

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
		return (lastTouchX != -1 && lastTouchY != -1);
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		lastTouchX = x;
		lastTouchY = y;
		return true;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		lastTouchX = -1;
		lastTouchY = -1;
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}

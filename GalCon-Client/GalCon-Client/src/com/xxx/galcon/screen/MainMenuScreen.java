package com.xxx.galcon.screen;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.xxx.galcon.Constants;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;

public class MainMenuScreen implements ScreenFeedback {
	private SpriteBatch spriteBatch;
	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();
	private String returnValue;

	Map<String, TouchRegion> touchRegions = new HashMap<String, TouchRegion>();

	public MainMenuScreen() {
		spriteBatch = new SpriteBatch();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
	}

	private void updateFont() {
		int width = Gdx.graphics.getWidth() / 2;
		int height = Gdx.graphics.getHeight() / 2;

		addText(Constants.JOIN, (int) (height * .4f), true, width, height);
		addText(Constants.CREATE, (int) (height * .31f), true, width, height);
		addText(Constants.CURRENT, (int) (height * .22f), true, width, height);
	}

	private String currentUserText() {
		return "(Level " + GameLoop.USER.rank + ")";
	}

	private void addText(String text, int y, boolean isTouchable, int screenWidth, int screenHeight) {
		BitmapFont font = Fonts.getInstance().largeFont();
		TextBounds fontBounds = font.getBounds(text);
		int x = screenWidth / 2 - (int) fontBounds.width / 2;
		font.draw(spriteBatch, text, x, y);

		if (isTouchable && touchRegions.size() != 3) {
			touchRegions.put(text, new TouchRegion(x, y, fontBounds.width, fontBounds.height, true));
		}
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		int width = Gdx.graphics.getWidth() / 2;
		int height = Gdx.graphics.getHeight() / 2;

		Integer touchX = null;
		Integer touchY = null;
		if (Gdx.input.isTouched()) {
			int x = Gdx.input.getX() / 2;
			int y = Gdx.input.getY() / 2;

			y = (int) height - y;
			touchX = x;
			touchY = y;
		}

		viewMatrix.setToOrtho2D(0, 0, width, height);
		spriteBatch.setProjectionMatrix(viewMatrix);
		spriteBatch.setTransformMatrix(transformMatrix);
		spriteBatch.begin();

		if (touchX != null) {
			for (Map.Entry<String, TouchRegion> touchRegionEntry : touchRegions.entrySet()) {
				TouchRegion touchRegion = touchRegionEntry.getValue();
				if (touchRegion.contains(touchX, touchY)) {
					returnValue = touchRegionEntry.getKey();
				}
			}
		}

		updateFont();

		String galcon = "GalCon";
		BitmapFont extraLargeFont = Fonts.getInstance().extraLargeFont();
		int x = width / 2 - (int) extraLargeFont.getBounds(galcon).width / 2;
		extraLargeFont.draw(spriteBatch, galcon, x, (int) (height * .7f));

		BitmapFont smallFont = Fonts.getInstance().smallFont();
		x = width / 2 - (int) smallFont.getBounds(currentUserText()).width / 2;
		smallFont.draw(spriteBatch, currentUserText(), x, (int) (height * .57f));

		spriteBatch.end();
	}

	@Override
	public void resize(int width, int height) {
		touchRegions.clear();
	}

	@Override
	public void show() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
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

	}

	@Override
	public void resetState() {
		returnValue = null;
	}

	@Override
	public Object getRenderResult() {
		return returnValue;
	}
}

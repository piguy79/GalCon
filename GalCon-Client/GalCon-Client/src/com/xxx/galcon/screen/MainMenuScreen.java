package com.xxx.galcon.screen;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.xxx.galcon.ScreenFeedback;

public class MainMenuScreen implements ScreenFeedback {
	private BitmapFontCache fontCache;
	private SpriteBatch spriteBatch;
	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();
	private String returnValue;

	Map<String, TouchRegion> touchRegions = new HashMap<String, TouchRegion>();

	public MainMenuScreen() {
		BitmapFont font = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_32.fnt"),
				Gdx.files.internal("data/fonts/tahoma_32.png"), false);

		fontCache = new BitmapFontCache(font, false);
		fontCache.getFont().getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		spriteBatch = new SpriteBatch();

		updateFontCache();
	}

	@Override
	public void dispose() {
		fontCache.getFont().dispose();
		spriteBatch.dispose();
	}

	private void updateFontCache() {
		int width = Gdx.graphics.getWidth() / 2;
		int height = Gdx.graphics.getHeight() / 2;

		fontCache.clear();
		touchRegions.clear();

		addText("Join", (int) (height * .4f), true, width, height);
		addText("Create", (int) (height * .33f), true, width, height);
		addText("Galcon", (int) (height * .7f), false, width, height);
	}

	private void addText(String text, int y, boolean isTouchable, int screenWidth, int screenHeight) {
		TextBounds fontBounds = fontCache.getFont().getBounds(text);
		int x = screenWidth / 2 - (int) fontBounds.width / 2;
		fontCache.addText(text, x, y);

		if (isTouchable) {
			touchRegions.put(text, new TouchRegion(x, y, fontBounds.width, fontBounds.height));
		}
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
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

		fontCache.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void resize(int width, int height) {
		updateFontCache();
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
	public Object getRenderResult() {
		return returnValue;
	}
}
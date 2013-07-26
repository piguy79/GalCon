package com.xxx.galcon.screen;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Map;
import com.xxx.galcon.model.Maps;

public class LevelSelectionScreen implements ScreenFeedback, UIConnectionResultCallback<Maps> {

	private AssetManager assetManager;
	private List<Map> allMaps;
	private SpriteBatch spriteBatch;

	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();

	private String loadingMessage = "Loading...";

	public LevelSelectionScreen(AssetManager assetManager) {
		this.assetManager = assetManager;
		this.spriteBatch = new SpriteBatch();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		float width = Gdx.graphics.getWidth() / 2;
		float height = Gdx.graphics.getHeight() / 2;

		viewMatrix.setToOrtho2D(0, 0, width, height);

		spriteBatch.setProjectionMatrix(viewMatrix);
		spriteBatch.setTransformMatrix(transformMatrix);
		spriteBatch.begin();
		spriteBatch.enableBlending();

		BitmapFont mediumFont = Fonts.getInstance().mediumFont();
		BitmapFont smallFont = Fonts.getInstance().smallFont();

		if (allMaps == null) {
			BitmapFont font = mediumFont;
			if (loadingMessage.length() > 15) {
				font = smallFont;
			}
			float halfFontWidth = font.getBounds(loadingMessage).width / 2;
			font.draw(spriteBatch, loadingMessage, width / 2 - halfFontWidth, height * .4f);
		} else {
			String text = "Loaded: " + allMaps.size();
			float halfFontWidth = mediumFont.getBounds(text).width / 2;
			mediumFont.draw(spriteBatch, text, width / 2 - halfFontWidth, height * .4f);
		}

		spriteBatch.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		UIConnectionWrapper.findAllMaps(this);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetState() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionResult(Maps result) {
		this.allMaps = result.allMaps;
	}

	@Override
	public void onConnectionError(String msg) {
		// TODO Auto-generated method stub

	}

}

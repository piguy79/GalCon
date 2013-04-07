package com.xxx.galcon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.http.ConnectionResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;

public class JoinScreen implements ScreenFeedback, ConnectionResultCallback<AvailableGames> {
	private BitmapFont font;
	private SpriteBatch spriteBatch;
	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();
	private GameBoard returnValue;
	private AvailableGames allGames;

	public JoinScreen() {
		font = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_16.fnt"),
				Gdx.files.internal("data/fonts/tahoma_16.png"), false);
		spriteBatch = new SpriteBatch();
	}

	@Override
	public void dispose() {
		font.dispose();
		spriteBatch.dispose();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		float width = Gdx.graphics.getWidth() / 2;
		float height = Gdx.graphics.getHeight() / 2;

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

		if (allGames == null) {
			return;
		}

		spriteBatch.setProjectionMatrix(viewMatrix);
		spriteBatch.setTransformMatrix(transformMatrix);
		spriteBatch.begin();
		spriteBatch.disableBlending();
		spriteBatch.setColor(Color.WHITE);
		spriteBatch.enableBlending();

		font.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

		float textY = 0.98f;
		for (GameBoard gameBoard : allGames.getAllGames()) {
			String text = gameBoard.players.toString();
			float halfFontWidth = font.getBounds(text).width / 2;
			font.draw(spriteBatch, text, width / 2 - halfFontWidth, height * textY);
			if (touchX != null && touchX >= width / 2 - halfFontWidth && touchX <= width / 2 + halfFontWidth) {
				if (touchY != null && touchY <= height * textY && touchY >= height * (textY - .03f)) {
					returnValue = gameBoard;
				}
			}

			textY -= 0.05f;
		}

		spriteBatch.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

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

	@Override
	public void result(AvailableGames result) {
		this.allGames = result;
	}
}

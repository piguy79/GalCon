package com.xxx.galcon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public class MainMenu implements Screen {
	private BitmapFont font;
	private SpriteBatch spriteBatch;
	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();
	private String returnValue;

	public MainMenu() {
		font = new BitmapFont(Gdx.files.internal("data/fonts/font16.fnt"), Gdx.files.internal("data/fonts/font16.png"),
				false);
		spriteBatch = new SpriteBatch();
	}

	@Override
	public boolean render(GL20 gl, Camera camera) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

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
		spriteBatch.setProjectionMatrix(viewMatrix);
		spriteBatch.setTransformMatrix(transformMatrix);
		spriteBatch.begin();
		spriteBatch.disableBlending();
		spriteBatch.setColor(Color.WHITE);
		spriteBatch.enableBlending();
		spriteBatch.setBlendFunction(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
		String text = "Join";
		float halfFontWidth = font.getBounds(text).width / 2;
		font.draw(spriteBatch, text, width / 2 - halfFontWidth, height * .4f);
		if (touchX != null && touchX >= width / 2 - halfFontWidth && touchX <= width / 2 + halfFontWidth) {
			if (touchY != null && touchY <= height * .4f && touchY >= height * .35f) {
				returnValue = "join";
			}
		}
		text = "Create";
		halfFontWidth = font.getBounds(text).width / 2;
		font.draw(spriteBatch, text, width / 2 - halfFontWidth, height * .33f);
		if (touchX != null && touchX >= width / 2 - halfFontWidth && touchX <= width / 2 + halfFontWidth) {
			if (touchY != null && touchY <= height * .33f && touchY >= height * .28f) {
				returnValue = "create";
			}
		}

		text = "GalCon";
		halfFontWidth = font.getBounds(text).width / 2;
		font.draw(spriteBatch, text, width / 2 - halfFontWidth, height * .69f);

		spriteBatch.end();

		if (returnValue != null) {
			return false;
		}

		return true;
	}

	@Override
	public Object getRenderReturnValue() {
		return returnValue;
	}

	@Override
	public void dispose() {
		font.dispose();
		spriteBatch.dispose();
	}
}

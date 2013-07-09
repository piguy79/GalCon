package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.Fonts;

public class TextOverlay extends Overlay {

	private String[] textLines;
	private String fontSize = "large";

	public TextOverlay(String text, String fontSize, AssetManager assetManager) {
		this(text, assetManager);
		this.fontSize = fontSize;
	}

	public TextOverlay(String text, AssetManager assetManager) {
		super(assetManager);
		this.textLines = text.split("\n");
	}

	@Override
	protected void doCustomRender(float delta, SpriteBatch spriteBatch) {
		BitmapFont font;
		if (fontSize.equals("large")) {
			font = Fonts.getInstance().largeFont();
		} else {
			font = Fonts.getInstance().mediumFont();
		}

		int lineHeight = (int) (Gdx.graphics.getHeight() * 0.1);
		int y = (int) (Gdx.graphics.getHeight() * 0.4f) + lineHeight * textLines.length;
		for (int i = 0; i < textLines.length; ++i) {
			String text = textLines[i];
			float width = font.getBounds(text).width;

			int x = (int) (Gdx.graphics.getWidth() / 2 - width / 2);
			font.draw(spriteBatch, text, x, y);

			y -= lineHeight;
		}
	}
}

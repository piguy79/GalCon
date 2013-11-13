package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.xxx.galcon.Fonts;

public class TextOverlay extends Overlay {

	private String[] textLines;
	private String fontSize = "large";

	private AssetManager assetManager;

	public TextOverlay(String text, String fontSize, TextureAtlas menusAtlas, AssetManager assetManager) {
		this(text, menusAtlas, true, assetManager);
		this.fontSize = fontSize;
	}

	public TextOverlay(String text, TextureAtlas menusAtlas, boolean displayOverlayTexture, AssetManager assetManager) {
		super(menusAtlas, displayOverlayTexture);
		this.textLines = text.split("\n");
		this.assetManager = assetManager;
	}

	@Override
	protected void doCustomRender(float delta, SpriteBatch spriteBatch) {
		BitmapFont font;
		if (fontSize.equals("large")) {
			font = Fonts.getInstance(assetManager).largeFont();
		} else if (fontSize.equals("medium")) {
			font = Fonts.getInstance(assetManager).mediumFont();
		} else {
			font = Fonts.getInstance(assetManager).smallFont();
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

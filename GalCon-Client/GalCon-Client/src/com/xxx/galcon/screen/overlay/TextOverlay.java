package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.xxx.galcon.Fonts;

public class TextOverlay extends Overlay {

	private String text;

	public TextOverlay(String text, AssetManager assetManager) {
		super(assetManager);
		this.text = text;
	}

	@Override
	protected void doCustomRender(float delta) {
		BitmapFont font = Fonts.getInstance().largeFont();

		float width = font.getBounds(text).width;

		int x = (int) (Gdx.graphics.getWidth() / 2 - width / 2);
		font.draw(getSpriteBatch(), text, x, (int) (Gdx.graphics.getHeight() * 0.4f));
	}
}

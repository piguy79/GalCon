package com.xxx.galcon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Fonts {

	private static Fonts instance = new Fonts();

	private BitmapFont smallFont;
	private BitmapFont mediumFont;

	private Fonts() {
		mediumFont = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_16.fnt"),
				Gdx.files.internal("data/fonts/tahoma_16.png"), false);
		mediumFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

		smallFont = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_16.fnt"),
				Gdx.files.internal("data/fonts/tahoma_16.png"), false);
		smallFont.scale(-0.3f);
		smallFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}

	public static Fonts getInstance() {
		return instance;
	}

	public BitmapFont smallFont() {
		return smallFont;
	}

	public BitmapFont mediumFont() {
		return mediumFont;
	}
}
package com.xxx.galcon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Fonts {

	private static Fonts instance = new Fonts();

	private BitmapFont smallFont;
	private BitmapFont mediumFont;
	private BitmapFont largeFont;
	private BitmapFont extraLargeFont;

	private Fonts() {
		mediumFont = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_16.fnt"),
				Gdx.files.internal("data/fonts/tahoma_16.png"), false);
		mediumFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

		smallFont = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_16.fnt"),
				Gdx.files.internal("data/fonts/tahoma_16.png"), false);
		smallFont.scale(-0.3f);
		smallFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

		largeFont = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_32.fnt"),
				Gdx.files.internal("data/fonts/tahoma_32.png"), false);
		largeFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

		extraLargeFont = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_32.fnt"),
				Gdx.files.internal("data/fonts/tahoma_32.png"), false);
		extraLargeFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		extraLargeFont.scale(0.5f);
	}

	public static Fonts getInstance() {
		if (instance == null) {
			instance = new Fonts();
		}
		return instance;
	}

	public static void dispose() {
		if (instance != null) {
			instance.smallFont.dispose();
			instance.extraLargeFont.dispose();
			instance.largeFont.dispose();
			instance.mediumFont.dispose();
		}

		instance = null;
	}

	public BitmapFont smallFont() {
		return smallFont;
	}

	public BitmapFont mediumFont() {
		return mediumFont;
	}

	public BitmapFont largeFont() {
		return largeFont;
	}

	public BitmapFont extraLargeFont() {
		return extraLargeFont;
	}
}

package com.xxx.galcon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Fonts {

	private static Fonts instance = new Fonts();

	private BitmapFont smallFont;
	private BitmapFont mediumFont;
	private BitmapFont largeFont;
	private BitmapFont extraLargeFont;

	private Fonts() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/crackdr2.ttf"));

		float width = Gdx.graphics.getWidth();
		float scaleFactor = width / 720.0f;

		smallFont = generator.generateFont((int) (12.0f * scaleFactor));
		mediumFont = generator.generateFont((int) (22.0f * scaleFactor));
		largeFont = generator.generateFont((int) (28.0f * scaleFactor));
		extraLargeFont = generator.generateFont((int) (36.0f * scaleFactor));

		generator.dispose();
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

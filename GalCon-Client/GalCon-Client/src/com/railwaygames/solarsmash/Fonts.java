package com.railwaygames.solarsmash;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Fonts {

	private static Fonts instance = null;

	private BitmapFont xSmallFont;
	private BitmapFont smallFont;
	private BitmapFont mediumFont;
	private BitmapFont largeFont;
	private BitmapFont xLargeFont;

	private Fonts() {
		float width = Gdx.graphics.getWidth();
		float scaleFactor = width / 680.0f;

		xSmallFont = new BitmapFont(Gdx.files.internal("data/fonts/copperplate_32.fnt"));
		xSmallFont.getRegion().getTexture().setFilter(Linear, Linear);
		xSmallFont.setScale(0.75f * scaleFactor);

		smallFont = new BitmapFont(Gdx.files.internal("data/fonts/copperplate_32.fnt"));
		smallFont.getRegion().getTexture().setFilter(Linear, Linear);
		smallFont.setScale(1.0f * scaleFactor);

		mediumFont = new BitmapFont(Gdx.files.internal("data/fonts/copperplate_32.fnt"));
		mediumFont.getRegion().getTexture().setFilter(Linear, Linear);
		mediumFont.setScale(1.2f * scaleFactor);

		largeFont = new BitmapFont(Gdx.files.internal("data/fonts/copperplate_32.fnt"));
		largeFont.getRegion().getTexture().setFilter(Linear, Linear);
		largeFont.setScale(3.0f * scaleFactor);

		xLargeFont = new BitmapFont(Gdx.files.internal("data/fonts/copperplate_32.fnt"));
		xLargeFont.getRegion().getTexture().setFilter(Linear, Linear);
		xLargeFont.setScale(9.0f * scaleFactor);
	}

	public static Fonts getInstance(AssetManager assetManager) {
		if (instance == null) {
			instance = new Fonts();
		}
		return instance;
	}

	public static void dispose() {
		if (instance != null) {
			instance.xSmallFont.dispose();
			instance.smallFont.dispose();
			instance.mediumFont.dispose();
			instance.largeFont.dispose();
			instance.xLargeFont.dispose();
			instance = null;
		}
	}

	public BitmapFont xSmallFont() {
		return xSmallFont;
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

	public BitmapFont xLargeFont() {
		return xLargeFont;
	}
}

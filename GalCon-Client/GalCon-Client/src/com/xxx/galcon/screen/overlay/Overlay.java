package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Overlay {

	private TextureRegion blackBackground;
	private SpriteBatch spriteBatch;
	private boolean displayOverlayTexture;

	public Overlay(TextureAtlas menusAtlas, boolean displayOverlayTexture) {
		blackBackground = menusAtlas.findRegion("transparent_square");
		spriteBatch = new SpriteBatch();
		this.displayOverlayTexture = displayOverlayTexture;
	}

	public void render(float delta) {
		spriteBatch.begin();

		if (displayOverlayTexture) {
			spriteBatch.draw(blackBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}

		doCustomRender(delta, spriteBatch);

		spriteBatch.end();
	}

	protected abstract void doCustomRender(float delta, SpriteBatch spriteBatch);

	public boolean isDisplayOverlayTexture() {
		return displayOverlayTexture;
	}

}

package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Overlay {

	private Texture blackBackground;
	private SpriteBatch spriteBatch;

	public Overlay(AssetManager assetManager) {
		blackBackground = assetManager.get("data/images/transparent_square.png", Texture.class);
		spriteBatch = new SpriteBatch();
	}

	public void render(float delta) {
		spriteBatch.begin();
		spriteBatch.draw(blackBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		doCustomRender(delta);

		spriteBatch.end();
	}

	protected abstract void doCustomRender(float delta);

	public SpriteBatch getSpriteBatch() {
		return spriteBatch;
	}
}

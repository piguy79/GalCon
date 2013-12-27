package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class Overlay extends Actor {

	private TextureRegion blackBackground;

	public Overlay(TextureAtlas menusAtlas) {
		blackBackground = menusAtlas.findRegion("transparent_square");
		this.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.draw(blackBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		doCustomRender(batch);
	}

	protected abstract void doCustomRender(SpriteBatch batch);
}

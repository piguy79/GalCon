package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class GameListHud extends Hud {

	public GameListHud(AssetManager assetManager) {
		super();

		addHudButton(new BackHudButton(assetManager.get("data/images/arrow_left.png", Texture.class)));

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
}

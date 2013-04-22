package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;

public class GameListHud extends Hud {

	public GameListHud(AssetManager assetManager) {
		super();

		addHudButton(new BackHudButton(assetManager));
		addHudButton(new RefreshHudButton(assetManager));

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
}

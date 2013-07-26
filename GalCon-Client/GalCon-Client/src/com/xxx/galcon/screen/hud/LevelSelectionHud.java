package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;

public class LevelSelectionHud extends Hud {

	public LevelSelectionHud(AssetManager assetManager) {
		super();

		addHudButton(new BackHudButton(assetManager));

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
}
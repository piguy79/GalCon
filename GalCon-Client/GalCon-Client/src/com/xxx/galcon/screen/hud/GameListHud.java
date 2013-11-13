package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class GameListHud extends Hud {

	public GameListHud(AssetManager assetManager) {
		super();

		TextureAtlas menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		
		addHudButton(new BackHudButton(menusAtlas));
		addHudButton(new RefreshHudButton(menusAtlas));

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
}

package com.xxx.galcon.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.UISkin;

public class CurrentGameScreen extends GameListScreen {

	public CurrentGameScreen(AssetManager assetManager, UISkin skin) {
		super(assetManager, skin);
	}

	@Override
	public void resetState() {
		super.resetState();
		UIConnectionWrapper.findCurrentGamesByPlayerHandle(this, GameLoop.USER.handle);
	}
}

package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.xxx.galcon.screen.Action;

public class BackHudButton extends HudButton {

	public BackHudButton(AssetManager assetManager) {
		super(assetManager.get("data/images/back.png", Texture.class));
	}

	@Override
	public Action getActionOnClick() {
		return Action.BACK;
	}

	@Override
	public void updateLocationAndSize(int screenWidth, int screenHeight) {
		int buttonHeight = (int) (Gdx.graphics.getHeight() * (HeaderHud.HEADER_HEIGHT_PERCENT * 0.88f));
		this.x = 10;
		this.y = screenHeight - buttonHeight - 5;
		this.width = buttonHeight;
		this.height = buttonHeight;
	}
}
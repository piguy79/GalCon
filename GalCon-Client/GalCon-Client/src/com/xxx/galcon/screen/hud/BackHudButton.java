package com.xxx.galcon.screen.hud;

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
		int buttonWidth = (int) (screenWidth * BUTTON_SIZE_RATIO);
		this.x = 10;
		this.y = screenHeight - buttonWidth - 5;
		this.width = buttonWidth;
		this.height = buttonWidth;
	}
}
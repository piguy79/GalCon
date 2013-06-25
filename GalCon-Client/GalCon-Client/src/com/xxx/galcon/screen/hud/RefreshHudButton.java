package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.xxx.galcon.screen.Action;

public class RefreshHudButton extends HudButton {
	public RefreshHudButton(AssetManager assetManager) {
		super(assetManager.get("data/images/refresh.png", Texture.class));
	}

	@Override
	public String getActionOnClick() {
		return Action.REFRESH;
	}

	@Override
	public void updateLocationAndSize(int screenWidth, int screenHeight) {
		int buttonHeight = (int) (Gdx.graphics.getHeight() * (HeaderHud.HEADER_HEIGHT_RATIO * 0.88f));
		this.x = screenWidth - buttonHeight - MARGIN;
		this.y = screenHeight - buttonHeight - 5;

		this.width = buttonHeight;
		this.height = buttonHeight;
	}
}
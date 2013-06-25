package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.xxx.galcon.screen.Action;

public class EndTurnHudButton extends HudButton {

	public EndTurnHudButton(AssetManager assetManager) {
		super(assetManager.get("data/images/end_turn.png", Texture.class));
	}

	@Override
	public String getActionOnClick() {
		return Action.END_TURN;
	}

	@Override
	public void updateLocationAndSize(int screenWidth, int screenHeight) {
		int buttonWidth = (int) (screenWidth * BUTTON_SIZE_RATIO);

		int midX = (int) (screenWidth * .9f);
		int midY = (int) (screenHeight * .06f);

		this.x = midX - buttonWidth / 2;
		this.y = midY - buttonWidth / 2;
		this.width = buttonWidth;
		this.height = buttonWidth;
	}
}
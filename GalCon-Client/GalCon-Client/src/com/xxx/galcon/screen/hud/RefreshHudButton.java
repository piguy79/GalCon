package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.graphics.Texture;
import com.xxx.galcon.screen.Action;

public class RefreshHudButton extends HudButton {
	public RefreshHudButton(Texture texture) {
		super(texture);
	}

	@Override
	public Action getActionOnClick() {
		return Action.REFRESH;
	}

	@Override
	public void updateLocationAndSize(int screenWidth, int screenHeight) {
		int buttonWidth = (int) (screenWidth * BUTTON_SIZE_RATIO);
		this.x = screenWidth - buttonWidth - MARGIN;
		this.y = MARGIN;
		this.width = buttonWidth;
		this.height = buttonWidth;
	}
}
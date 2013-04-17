package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.graphics.Texture;
import com.xxx.galcon.screen.Action;

public class SendHudButton extends HudButton {

	public SendHudButton(Texture texture) {
		super(texture);
	}

	@Override
	public Action getActionOnClick() {
		return Action.SEND;
	}

	@Override
	public void updateLocationAndSize(int screenWidth, int screenHeight) {
		int buttonWidth = (int) (screenWidth * BUTTON_SIZE_RATIO);
		this.x = MARGIN;
		this.y = MARGIN + buttonWidth + MARGIN;
		this.width = buttonWidth;
		this.height = buttonWidth;
	}
}
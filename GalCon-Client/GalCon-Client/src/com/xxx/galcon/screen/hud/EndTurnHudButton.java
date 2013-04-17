package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.graphics.Texture;
import com.xxx.galcon.screen.Action;

public class EndTurnHudButton extends HudButton {

	public EndTurnHudButton(Texture texture) {
		super(texture);
	}

	@Override
	public Action getActionOnClick() {
		return Action.END_TURN;
	}

	@Override
	public void updateLocationAndSize(int screenWidth, int screenHeight) {
		int buttonWidth = (int) (screenWidth * BUTTON_SIZE_RATIO);
		this.x = screenWidth - buttonWidth - MARGIN;
		this.y = MARGIN + buttonWidth + MARGIN + 15;
		this.width = buttonWidth;
		this.height = (int) (buttonWidth * 0.6f);
	}
}
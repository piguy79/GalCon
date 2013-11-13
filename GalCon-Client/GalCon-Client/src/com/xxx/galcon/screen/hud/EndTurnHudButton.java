package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.xxx.galcon.screen.Action;

public class EndTurnHudButton extends Button {

	public EndTurnHudButton(TextureAtlas atlas) {
		super(atlas.findRegion("end_turn"));
	}

	@Override
	public String getActionOnClick() {
		return Action.END_TURN;
	}

	@Override
	public void updateLocationAndSize(int x, int y, int screenWidth, int screenHeight) {
		int buttonWidth = (int) (screenWidth * BUTTON_SIZE_RATIO);

		int midX = (int) (screenWidth * .9f);
		int midY = (int) (screenHeight * .06f);

		this.x = midX - buttonWidth / 2;
		this.y = midY - buttonWidth / 2;
		this.width = buttonWidth;
		this.height = buttonWidth;
	}
}
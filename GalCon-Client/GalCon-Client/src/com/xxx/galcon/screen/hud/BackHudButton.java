package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.xxx.galcon.screen.Action;

public class BackHudButton extends Button {

	public BackHudButton(TextureAtlas atlas) {
		super(atlas.findRegion("back"));
	}

	@Override
	public String getActionOnClick() {
		return Action.BACK;
	}

	@Override
	public void updateLocationAndSize(int x, int y, int screenWidth, int screenHeight) {
		int buttonHeight = (int) (Gdx.graphics.getHeight() * (HeaderHud.HEADER_HEIGHT_RATIO * 0.88f));
		this.x = 10;
		this.y = screenHeight - buttonHeight - 5;
		this.width = buttonHeight;
		this.height = buttonHeight;
	}
}
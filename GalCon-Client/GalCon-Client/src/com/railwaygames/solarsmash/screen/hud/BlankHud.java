package com.railwaygames.solarsmash.screen.hud;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.screen.Resources;

public class BlankHud extends Group {
	private Resources resources;
	private AtlasRegion bgRegion;

	public BlankHud(Resources resources, float width, float height) {
		this.resources = resources;

		setHeight(height);
		setWidth(width);

		createBackground(true);
	}

	private void createBackground(boolean basic) {
		String image = "player_hud";
		bgRegion = resources.gameBoardAtlas.findRegion(image);
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}
}

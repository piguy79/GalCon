package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.screen.Resources;

public class RoundInformationTopHud extends Group {
	private Resources resources;

	private AtlasRegion bgRegion;

	public RoundInformationTopHud(Resources resources, float width, float height) {
		this.resources = resources;

		setHeight(height);
		setWidth(width);

		createBackground();
	}

	private void createBackground() {
		bgRegion = resources.gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}
}

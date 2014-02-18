package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Overlay extends Group {

	public Overlay(TextureAtlas menusAtlas) {
		TextureRegion blackBackground = menusAtlas.findRegion("transparent_square");
		Image backGround = new Image(new TextureRegionDrawable(blackBackground));
		backGround.setFillParent(true);
		backGround.setColor(0, 0, 0, 0.8f);

		addActor(backGround);
	}
}

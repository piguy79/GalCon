package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public abstract class Overlay extends Group {

	private TextureRegion blackBackground;

	public Overlay(TextureAtlas menusAtlas) {
		blackBackground = menusAtlas.findRegion("transparent_square");
		Image backGround = new Image(new TextureRegionDrawable(blackBackground));
		backGround.setWidth(Gdx.graphics.getWidth());
		backGround.setHeight(Gdx.graphics.getHeight());
		backGround.setColor(0, 0, 0, 0.6f);
				
		addActor(backGround);
	}


}

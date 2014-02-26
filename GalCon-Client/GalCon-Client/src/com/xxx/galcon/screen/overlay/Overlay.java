package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.screen.Resources;

public class Overlay extends Group {

	private Image backGround;

	public Overlay(Resources resources) {
		TextureRegion blackBackground = resources.menuAtlas.findRegion("transparent_square");
		backGround = new Image(new TextureRegionDrawable(blackBackground));
		backGround.setColor(0, 0, 0, 0.8f);
		backGround.setWidth(Gdx.graphics.getWidth());
		backGround.setHeight(Gdx.graphics.getHeight());

		addActor(backGround);
	}

	@Override
	public void setBounds(float x, float y, float width, float height) {
		super.setBounds(x, y, width, height);

		backGround.setWidth(width);
		backGround.setHeight(height);
	}

	@Override
	public void setWidth(float width) {
		super.setWidth(width);

		backGround.setWidth(width);
	}

	@Override
	public void setHeight(float height) {
		super.setHeight(height);

		backGround.setHeight(height);
	}
}

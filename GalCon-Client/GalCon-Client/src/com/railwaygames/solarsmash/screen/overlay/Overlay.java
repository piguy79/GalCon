package com.railwaygames.solarsmash.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.screen.Resources;

public class Overlay extends Group {

	protected Image backGround;
	protected Resources resources;
	private float alpha = 0.8f;
	private final Color defaultColor = new Color(0, 0, 0, alpha);

	public Overlay(Resources resources) {
		this.resources = resources;

		addBackground(defaultColor);
	}

	public Overlay(Resources resources, float alpha) {
		this(resources);
		this.alpha = alpha;
	}

	public void clear() {
		this.clear(defaultColor);
	}

	public void clear(Color color) {
		super.clear();
		addBackground(color);
	}

	private void addBackground(Color color) {
		TextureRegion blackBackground = resources.menuAtlas.findRegion("transparent_square");
		backGround = new Image(new TextureRegionDrawable(blackBackground));
		backGround.setColor(color);
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

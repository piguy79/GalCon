package com.railwaygames.solarsmash.screen.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class BasicTextHud extends Group {
	private Resources resources;
	private AtlasRegion bgRegion;
	private ShaderLabel label;

	public BasicTextHud(Resources resources, float width, float height) {
		this.resources = resources;

		setHeight(height);
		setWidth(width);

		createBackground(true);

		label = new ShaderLabel(resources.fontShader, "", resources.skin, Constants.UI.SMALL_FONT, Color.WHITE);
	}

	private void createBackground(boolean basic) {
		String image = "player_hud";
		bgRegion = resources.gameBoardAtlas.findRegion(image);
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}

	public void addText(String text) {
		label.setText(text);

		TextBounds bounds = label.getTextBounds();
		label.setX(getWidth() * 0.02f);
		label.setY(0);
		label.setHeight(getHeight());
		label.setWidth(getWidth() - getWidth() * 0.04f);
		label.setWrap(true);
		label.setAlignment(Align.center, Align.center);
		addActor(label);
	}
}

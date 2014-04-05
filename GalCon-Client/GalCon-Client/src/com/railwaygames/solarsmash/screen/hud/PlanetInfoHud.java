package com.railwaygames.solarsmash.screen.hud;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class PlanetInfoHud extends Group {
	private Resources resources;
	private ShaderLabel regenLabel;

	private AtlasRegion bgRegion;

	public PlanetInfoHud(Resources resources, float width, float height) {
		this.resources = resources;

		setHeight(height);
		setWidth(width);

		createBackground();
		createLabels();
	}

	private void createLabels() {
		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "Regen rate", resources.skin,
					Constants.UI.DEFAULT_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getX());
			label.setY(getHeight() * 0.7f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.4f);
			label.setAlignment(Align.center, Align.center);
			addActor(label);
		}
		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "0", resources.skin, Constants.UI.LARGE_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getX());
			label.setY(getHeight() * 0.15f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.4f);
			label.setAlignment(Align.center, Align.center);
			addActor(label);

			regenLabel = label;
		}
	}

	private void createBackground() {
		bgRegion = resources.gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}

	public void updateRegen(int value) {
		regenLabel.setText("" + value);
	}
}

package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.event.HarvestEvent;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class PlanetInfoHud extends Group {
	private Resources resources;
	private ShaderLabel regenLabel;

	private AtlasRegion bgRegion;
	private Planet planet;
	private GameBoard gameBoard;

	private ImageButton harvestButton;
	private ShaderLabel harvestText;

	public PlanetInfoHud(Planet planet, GameBoard gameBoard, Resources resources, float width, float height) {
		this.resources = resources;
		this.planet = planet;
		this.gameBoard = gameBoard;

		setHeight(height);
		setWidth(width);

		createBackground();
		createLabels();

		regenLabel.setText("" + (int) planet.regen);

		if (isHarvestAvailable()) {
			createHarvestButton(resources.fontShader, resources.skin);
		}
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

	private void createHarvestButton(ShaderProgram fontShader, UISkin skin) {
		harvestButton = new ImageButton(skin, Constants.UI.BASIC_BUTTON);
		harvestButton.setLayoutEnabled(false);
		float bWidth = getWidth() * 0.3f;
		float bHeight = bWidth * 0.4f;
		harvestButton.setBounds(getWidth() * 0.75f - bWidth * 0.5f, getHeight() * 0.5f - bHeight * 0.5f, bWidth,
				bHeight);

		harvestText = new ShaderLabel(fontShader, "Harvest", skin, Constants.UI.BASIC_BUTTON_TEXT);
		harvestText.setAlignment(Align.center);
		harvestText.setBounds(getWidth() * 0.75f - bWidth * 0.5f, getHeight() * 0.5f - bHeight * 0.5f, bWidth, bHeight);
		harvestText.setY(harvestText.getY() + getHeight() * 0.05f);

		addActor(harvestButton);
		addActor(harvestText);

		harvestButton.addListener(harvestListener);
		harvestText.addListener(harvestListener);
	}

	private HarvestListener harvestListener = new HarvestListener();

	private class HarvestListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			fire(new HarvestEvent(planet));
		}
	}

	private void createBackground() {
		bgRegion = resources.gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}

	private boolean isHarvestAvailable() {
		return !planet.isUnderHarvest() && planet.hasAbility() && planet.isAlive()
				&& !GameLoop.USER.hasMoved(gameBoard) && planet.isOwnedBy(GameLoop.USER.handle);
	}
}

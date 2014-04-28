package com.railwaygames.solarsmash.screen.hud;

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
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.UIConnectionWrapper;
import com.railwaygames.solarsmash.UISkin;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.Map;
import com.railwaygames.solarsmash.model.Maps;
import com.railwaygames.solarsmash.model.Planet;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.event.HarvestEvent;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class PlanetInfoHud extends Group {
	private Resources resources;

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
		createRegenLabels(planet.isOwned(), resources, (int) planet.regen, getWidth(), getHeight(), this);

		loadMaps();
	}

	public static void createUnderHarvestLabel(Resources resources, GameBoard gameBoard, Planet planet, float width,
			float height, Group parent) {
		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "Rounds remaining with\nharvest bonus",
					resources.skin, Constants.UI.X_SMALL_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(width * 0.4f);
			label.setY(height * 0.75f - bounds.height * 0.5f);
			label.setWidth(width * 0.6f);
			label.setAlignment(Align.center, Align.center);
			parent.addActor(label);
		}
		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, ""
					+ planet.roundsUntilHarvestIsComplete(gameBoard), resources.skin, Constants.UI.LARGE_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(width * 0.4f);
			label.setY(height * 0.15f - bounds.height * 0.5f);
			label.setWidth(width * 0.6f);
			label.setAlignment(Align.center, Align.center);
			parent.addActor(label);
		}
	}

	public static void createRegenLabels(boolean isOwned, Resources resources, int regen, float width, float height,
			Group parent) {
		{
			String fontSize = Constants.UI.SMALL_FONT;
			String text = "Ship build rate";

			ShaderLabel label = new ShaderLabel(resources.fontShader, text, resources.skin, fontSize);
			TextBounds bounds = label.getTextBounds();
			label.setX(0);
			label.setY(height * 0.7f - bounds.height * 0.5f);
			label.setWidth(width * 0.4f);
			label.setAlignment(Align.center, Align.center);
			label.setWrap(true);
			parent.addActor(label);
		}
		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "" + regen, resources.skin,
					Constants.UI.LARGE_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(0);
			label.setY(height * 0.15f - bounds.height * 0.5f);
			label.setWidth(width * 0.4f);
			label.setAlignment(Align.center, Align.center);
			parent.addActor(label);
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

	private void loadMaps() {
		UIConnectionWrapper.findAllMaps(new UIConnectionResultCallback<Maps>() {

			@Override
			public void onConnectionResult(Maps result) {
				Map gameMap = null;
				for (Map map : result.allMaps) {
					if (map.key == gameBoard.map) {
						gameMap = map;
					}
				}

				if (gameMap != null && gameMap.canHarvest && isHarvestAvailable()) {
					createHarvestButton(resources.fontShader, resources.skin);
				} else if (planet.isUnderHarvest()) {
					PlanetInfoHud.createUnderHarvestLabel(resources, gameBoard, planet, getWidth(), getHeight(),
							PlanetInfoHud.this);
				}
			}

			@Override
			public void onConnectionError(String msg) {

			}
		});
	}

	private boolean isHarvestAvailable() {
		return !planet.isUnderHarvest() && planet.hasAbility() && planet.isAlive()
				&& !GameLoop.USER.hasMoved(gameBoard) && planet.isOwnedBy(GameLoop.USER.handle);
	}
}

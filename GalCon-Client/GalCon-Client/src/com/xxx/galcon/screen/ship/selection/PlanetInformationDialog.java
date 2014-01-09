package com.xxx.galcon.screen.ship.selection;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.xxx.galcon.Constants;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.model.display.AbilityDisplay;
import com.xxx.galcon.model.factory.PlanetButtonFactory;
import com.xxx.galcon.screen.widget.CancelEnabledDialog;
import com.xxx.galcon.screen.widget.PlanetButton;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class PlanetInformationDialog extends CancelEnabledDialog {
	
	private Planet planet;
	private GameBoard gameboard;
	private boolean animated;
	private UISkin skin;
	private ShaderProgram fontShader;
	
	private PlanetButton planetImage;

	public PlanetInformationDialog(AssetManager assetManager, float width,
			float height, Stage stage, Planet planet, GameBoard gameboard, boolean animated, ShaderProgram fontShader, UISkin skin) {
		super(assetManager, width, height, stage, skin);
		this.planet = planet;
		this.gameboard = gameboard;
		this.animated = animated;
		this.skin = skin;
		this.fontShader = fontShader;
		createPlanetImage();
		createLabels();
	}

	private void createLabels() {
		ShaderLabel regenName = new ShaderLabel(fontShader, "Regen Rate:", skin, Constants.UI.DEFAULT_FONT);
		ShaderLabel regenRate = new ShaderLabel(fontShader, Math.round(planet.shipRegenRate) + "", skin, Constants.UI.DEFAULT_FONT);
		ShaderLabel populationName = new ShaderLabel(fontShader, "Population:", skin, Constants.UI.DEFAULT_FONT);
		ShaderLabel populationValue = new ShaderLabel(fontShader, fakePopulation(planet), skin, Constants.UI.DEFAULT_FONT);
		ShaderLabel abilityName = new ShaderLabel(fontShader, "Ability:      ", skin, Constants.UI.DEFAULT_FONT);
		ShaderLabel abilityValue = new ShaderLabel(fontShader, AbilityDisplay.abilityDisplayNames.get(planet.ability), skin, Constants.UI.DEFAULT_FONT);

		float padNameToValue = getWidth() * 0.05f;
		float initialPadX = getWidth() * 0.02f;
		
		positionAndPlaceActor(regenName, new Point(initialPadX, planetImage.getY() - (regenName.getTextBounds().height * 2)));
		positionAndPlaceActor(regenRate, new Point(regenName.getX() + regenName.getTextBounds().width + padNameToValue, regenName.getY()));
		positionAndPlaceActor(populationName, new Point(initialPadX, regenName.getY() - (regenName.getTextBounds().height)));
		positionAndPlaceActor(populationValue, new Point(populationName.getX() + populationName.getTextBounds().width + padNameToValue, populationName.getY()));
		
		if(planet.hasAbility()){
			positionAndPlaceActor(abilityName, new Point(initialPadX, populationName.getY() - (populationName.getTextBounds().height)));
			positionAndPlaceActor(abilityValue, new Point(abilityName.getX() + abilityName.getTextBounds().width + padNameToValue, abilityName.getY()));
		}
		
	}

	private CharSequence fakePopulation(Planet planet) {
		double value = Math.random() * planet.shipRegenRate;
		return Math.round(value) + ",000,000";
	}

	private void positionAndPlaceActor(ShaderLabel actor, Point position) {
		actor.setX(position.x);
		actor.setY(position.y);
		addActor(actor);
	}

	private void createPlanetImage() {
		PlanetButtonFactory.setup(assetManager, getWidth() * 0.4f, getWidth() * 0.4f);
		planetImage = PlanetButtonFactory.createPlanetButton(planet, gameboard, animated, getWidth() * 0.4f, getWidth() * 0.4f);
		planetImage.setX((getWidth() / 2) - (planetImage.getWidth()  /2));
		planetImage.setY(getHeight() - (planetImage.getHeight() + (planetImage.getHeight() * 0.2f)));
		
		addActor(planetImage);
	}

	@Override
	public void hide() {
		super.hide(0.4f);
	}



}

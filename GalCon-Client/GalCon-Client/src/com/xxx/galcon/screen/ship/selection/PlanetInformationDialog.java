package com.xxx.galcon.screen.ship.selection;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.model.display.AbilityDisplay;
import com.xxx.galcon.model.factory.PlanetButtonFactory;
import com.xxx.galcon.screen.event.HarvestEvent;
import com.xxx.galcon.screen.widget.ActionButton;
import com.xxx.galcon.screen.widget.CancelEnabledDialog;
import com.xxx.galcon.screen.widget.Moon;
import com.xxx.galcon.screen.widget.PlanetButton;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class PlanetInformationDialog extends CancelEnabledDialog {
	
	private Planet planet;
	private GameBoard gameboard;
	private boolean animated;
	private UISkin skin;
	private ShaderProgram fontShader;
	
	private PlanetButton planetImage;
	private int offset;
	private ActionButton harvestButton;

	public PlanetInformationDialog(AssetManager assetManager, float width,
			float height, Stage stage, Planet planet, GameBoard gameboard, boolean animated, ShaderProgram fontShader, UISkin skin, int offset) {
		super(assetManager, width, height, stage, skin);
		this.planet = planet;
		this.gameboard = gameboard;
		this.animated = animated;
		this.skin = skin;
		this.fontShader = fontShader;
		this.offset = offset;
		createPlanetImage();
		if(planet.hasAbility()){
			createMoonImage();
		}
		createLabels();
		createHarvestButton();
	}

	private void createMoonImage() {
		float sizeBase = Math.max(getHeight(), getWidth());
		float size = sizeBase * 0.15f;
		Moon moon = PlanetButtonFactory.createMoon(assetManager, planet, size, size);
		moon.setX((planetImage.getX() + (planetImage.getWidth() * 1.1f)) - (size / 2));
		moon.setY((planetImage.getY() + (planetImage.getHeight() * 0.7f)) - (size / 2));
		addActor(moon);
		
	}

	private void createLabels() {
		ShaderLabel regenName = new ShaderLabel(fontShader, "Regen Rate: ", skin, Constants.UI.DEFAULT_FONT_BLACK);
		ShaderLabel regenRate = new ShaderLabel(fontShader, Math.round(planet.shipRegenRate) + "", skin, Constants.UI.DEFAULT_FONT);
		ShaderLabel populationName = new ShaderLabel(fontShader, "Population: ", skin, Constants.UI.DEFAULT_FONT_BLACK);
		ShaderLabel populationValue = new ShaderLabel(fontShader, "" + Math.round(planet.population), skin, Constants.UI.DEFAULT_FONT);
		ShaderLabel abilityName = new ShaderLabel(fontShader, "Ability: ", skin, Constants.UI.DEFAULT_FONT_BLACK);
		ShaderLabel abilityValue = new ShaderLabel(fontShader, AbilityDisplay.abilityDisplayNames.get(planet.ability), skin, Constants.UI.DEFAULT_FONT);
		ShaderLabel harvestName = new ShaderLabel(fontShader, "Rounds until Death:", skin, Constants.UI.DEFAULT_FONT_BLACK);
		ShaderLabel harvestValue = new ShaderLabel(fontShader, "" + findRoundsRemainingInHarvest(), skin, Constants.UI.DEFAULT_FONT);

		float padNameToValue = getWidth() * 0.05f;
		float initialPadX = getWidth() * 0.02f;
		float yPad = getHeight() * 0.1f;
		
		positionAndPlaceActor(regenName, new Point(initialPadX, planetImage.getY() - (regenName.getTextBounds().height * 2)));
		positionAndPlaceActor(regenRate, new Point(regenName.getX() + regenName.getTextBounds().width + padNameToValue, regenName.getY()));
		positionAndPlaceActor(populationName, new Point(initialPadX, regenName.getY() - yPad));
		positionAndPlaceActor(populationValue, new Point(populationName.getX() + populationName.getTextBounds().width + padNameToValue, populationName.getY()));
		
		if(planet.hasAbility()){
			positionAndPlaceActor(abilityName, new Point(initialPadX, populationName.getY() - yPad));
			positionAndPlaceActor(abilityValue, new Point(abilityName.getX() + abilityName.getTextBounds().width + padNameToValue, abilityName.getY()));
		}
		
		if(planet.isUnderHarvest()){
			positionAndPlaceActor(harvestName, new Point(initialPadX, abilityName.getY() - yPad));
			positionAndPlaceActor(harvestValue, new Point(harvestName.getX() + harvestName.getTextBounds().width + padNameToValue, harvestName.getY()));

		}
		
	}

	private int findRoundsRemainingInHarvest() {
		if(planet.isUnderHarvest()){
			String harvestRounds= gameboard.gameConfig.getValue("harvestRounds");
			int roundNumber = Integer.parseInt(harvestRounds);
			return roundNumber - (gameboard.roundInformation.currentRound - planet.harvest.startingRound);
		}
		
		return -1;
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
		float sizeBase = getWidth() < getHeight() ? getWidth() : getHeight();
		PlanetButtonFactory.setup(assetManager, sizeBase * 0.4f, sizeBase * 0.4f, skin);
		planetImage = PlanetButtonFactory.createPlanetButton(planet, gameboard, animated, sizeBase * 0.4f, sizeBase * 0.4f);
		planetImage.setX((getWidth() / 2) - (planetImage.getWidth()  /2));
		planetImage.setY(getHeight() - (planetImage.getHeight() + (planetImage.getHeight() * 0.2f)));
		planetImage.setText(new StringBuilder().append(planet.numberOfShips - offset).toString());
		
		addActor(planetImage);
	}

	
	private void createHarvestButton(){
		Point position = new Point(getWidth() - (cancelButton.getWidth() * 0.6f), cancelButton.getY());
		harvestButton =  new ActionButton(skin,"okButton", position);
		harvestButton.setColor(0, 0, 0, 0);
		if(isHarvestAvailable()){
			harvestButton.setDisabled(true);
		}
		
		harvestButton.addListener(new ClickListener(){@Override
		public void clicked(InputEvent event, float x, float y) {
			
			if(isHarvestAvailable()){
				fire(new HarvestEvent(planet));
				hide();
			}
		}});
		addActor(harvestButton);
	}

	private boolean isHarvestAvailable() {
		return !planet.isUnderHarvest() && planet.hasAbility() && planet.isAlive() && !GameLoop.USER.hasMoved(gameboard) && planet.isOwnedBy(GameLoop.USER);
	}
	
	public void displayButtons(){
		harvestButton.addAction(Actions.fadeIn(0.4f));
	}
	
	@Override
	public void show(final Point point, float duration){
		addAction(Actions.sequence(new RunnableAction(){@Override
		public void run() {
			showParent(point, 0.4f);
		}}, Actions.delay(0.4f), new RunnableAction(){@Override
		public void run() {
			displayButtons();
		}}));
	}
	
	@Override
	public void doHide(){
		harvestButton.addAction(Actions.sequence(Actions.fadeOut(0.4f), new RunnableAction(){@Override
		public void run() {
			hideParent(0.4f);
		}}));
	}
	
	public void showParent(Point point, float duration){
		super.show(point, duration);
	}
	
	public void hideParent(float duration){
		super.hide(duration);
	}



}

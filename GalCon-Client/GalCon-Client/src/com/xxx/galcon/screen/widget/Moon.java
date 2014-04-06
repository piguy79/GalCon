package com.xxx.galcon.screen.widget;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.BoardScreen.BoardCalculations;
import com.xxx.galcon.screen.Resources;

public class Moon extends Group {

	public float angle = 0;
	public double rateOfOrbit = 0;
	public PlanetButton associatedPlanetButton;
	private Image harvestBeamImage;
	private Image moonHarvest;
	private Image moonHighlight;
	private Resources resources;

	public Moon(Resources resources, PlanetButton associatedPlanetButton, float height, float width) {
		this.resources = resources;
		TextureRegionDrawable moon = new TextureRegionDrawable(resources.planetAtlas.findRegion("moon"));

		moon.setMinHeight(height);
		moon.setMinWidth(width);
		addActor(new Image(moon));
		this.associatedPlanetButton = associatedPlanetButton;
		setupMovementSpeedAndDirection();

		setWidth(width);
		setHeight(height);
	}

	private void setupMovementSpeedAndDirection() {
		rateOfOrbit = Math.random();
		if (rateOfOrbit < 0.4) {
			rateOfOrbit = 0.5;
		}

		if (associatedPlanetButton.planet.regen <= 3) {
			angle = 360;
			rateOfOrbit = rateOfOrbit * -1;
		}
	}

	public void updateLocation(Group boardTable, BoardCalculations boardCalcs, Point point) {
		setX(point.x - getWidth() * 0.5f);
		setY(point.y - getHeight() * 0.5f);

		Planet planet = associatedPlanetButton.planet;
		if (planet.isAlive()) {
			if (planet.isUnderHarvest() || (!associatedPlanetButton.planet.owner.equals(Constants.OWNER_NO_ONE))) {
				Point start = associatedPlanetButton.centerPoint();
				float width = (float) Math.sqrt(Math.pow(point.x - start.x, 2) + Math.pow(point.y - start.y, 2));

				if (harvestBeamImage == null) {
					Action action;
					if (associatedPlanetButton.planet.isUnderHarvest()) {
						action = forever(sequence(color(Color.WHITE, 0.5f), color(Color.RED, 0.5f)));
					} else {
						action = forever(sequence(color(Color.WHITE, 3.0f), color(Color.BLUE, 3.0f)));
					}
					harvestBeamImage = new Image(new TextureRegionDrawable(
							resources.planetAtlas.findRegion("moon-harvest-beam")));
					float height = boardCalcs.getTileSize().height * 0.17f;

					harvestBeamImage.setBounds(start.x, start.y - height * 0.5f, width, height);
					harvestBeamImage.setOrigin(0, 0 + height * 0.5f);
					boardTable.addActorAt(2, harvestBeamImage);

					harvestBeamImage.addAction(action);
				}
				if (moonHarvest == null) {
					moonHarvest = new Image(new TextureRegionDrawable(
							resources.planetAtlas.findRegion("moon-harvest-1")));
					moonHarvest.setBounds(0, 0, getWidth(), getHeight());
					// addActor(moonHarvest);
				}
				if (moonHighlight == null) {
					Action action;
					if (associatedPlanetButton.planet.isUnderHarvest()) {
						action = forever(sequence(color(Color.YELLOW, 0.5f), color(Color.RED, 0.5f)));
					} else {
						action = forever(sequence(color(Color.WHITE, 3.0f), color(Color.BLUE, 3.0f)));
					}
					moonHighlight = new Image(
							new TextureRegionDrawable(resources.planetAtlas.findRegion("planet-glow")));
					moonHighlight.setBounds(0, 0, getWidth(), getHeight());
					moonHighlight.setColor(Color.YELLOW);
					addActorAt(0, moonHighlight);

					moonHighlight.addAction(action);
				}

				harvestBeamImage.setWidth(Math.abs(width));

				float trueAngle = MathUtils.atan2(point.y - start.y, point.x - start.x);
				harvestBeamImage.setRotation(MathUtils.radiansToDegrees * trueAngle);
			}
		}
	}
}

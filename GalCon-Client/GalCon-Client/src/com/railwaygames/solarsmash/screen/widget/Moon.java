package com.railwaygames.solarsmash.screen.widget;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.BoardScreen.BoardCalculations;

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
		TextureRegionDrawable moon;
		if (associatedPlanetButton.planet.ability.equals(Constants.ABILITY_ATTACK_INCREASE)) {
			moon = new TextureRegionDrawable(resources.planetAtlas.findRegion("moon"));

		} else if (associatedPlanetButton.planet.ability.equals(Constants.ABILITY_DEFENCE_INCREASE)) {
			moon = new TextureRegionDrawable(resources.planetAtlas.findRegion("moon"));

		} else {
			moon = new TextureRegionDrawable(resources.planetAtlas.findRegion("moon"));
		}
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

		if (associatedPlanetButton.planet.isUnderHarvest()) {
			Point start = associatedPlanetButton.centerPoint();
			float width = (float) Math.sqrt(Math.pow(point.x - start.x, 2) + Math.pow(point.y - start.y, 2));

			if (harvestBeamImage == null) {
				Action action = forever(sequence(color(Color.WHITE, 0.5f), color(Color.RED, 0.5f)));
				harvestBeamImage = new Image(new TextureRegionDrawable(
						resources.planetAtlas.findRegion("moon-harvest-beam")));
				float height = boardCalcs.getTileSize().height * 0.17f;

				harvestBeamImage.setBounds(start.x, start.y - height * 0.5f, width, height);
				harvestBeamImage.setOrigin(0, 0 + height * 0.5f);
				boardTable.addActorAt(1, harvestBeamImage);

				harvestBeamImage.addAction(action);
			}
			if (moonHarvest == null) {
				moonHarvest = new Image(new TextureRegionDrawable(resources.planetAtlas.findRegion("moon-harvest-1")));
				moonHarvest.setBounds(0, 0, getWidth(), getHeight());
				// addActor(moonHarvest);
			}
			if (moonHighlight == null) {
				Action action = forever(sequence(color(Color.YELLOW, 0.5f), color(Color.RED, 0.5f)));
				moonHighlight = new Image(new TextureRegionDrawable(resources.planetAtlas.findRegion("planet-glow")));
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

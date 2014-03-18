package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.screen.Resources;

public class Moon extends Image {

	public float angle = 0;
	public double rateOfOrbit = 0;
	public Planet associatedPlanet;

	public Moon(Resources resources, Planet associatedPlanet, float height, float width) {
		TextureRegionDrawable moon;
		if (associatedPlanet.ability.equals(Constants.ABILITY_ATTACK_INCREASE)) {
			moon = new TextureRegionDrawable(resources.planetAtlas.findRegion("moon"));

		} else if (associatedPlanet.ability.equals(Constants.ABILITY_DEFENCE_INCREASE)) {
			moon = new TextureRegionDrawable(resources.planetAtlas.findRegion("moon"));

		} else {
			moon = new TextureRegionDrawable(resources.planetAtlas.findRegion("moon"));
		}
		moon.setMinHeight(height);
		moon.setMinWidth(width);
		setDrawable(moon);
		this.associatedPlanet = associatedPlanet;
		setupMovementSpeedAndDirection();

	}

	private void setupMovementSpeedAndDirection() {

		rateOfOrbit = Math.random();
		if (rateOfOrbit < 0.4) {
			rateOfOrbit = 0.5;
		}

		if (associatedPlanet.shipRegenRate <= 3) {
			angle = 360;
			rateOfOrbit = rateOfOrbit * -1;
		}
	}

}

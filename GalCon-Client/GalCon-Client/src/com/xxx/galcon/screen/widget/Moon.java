package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.model.Planet;

public class Moon extends Image{
	
	public float angle = 0;
	public double rateOfOrbit = 0;
	public Planet associatedPlanet;
	
	public Moon(AssetManager assetManager, Planet associatedPlanet, float height, float width){
		TextureAtlas planetAtlas = assetManager.get("data/images/planets.atlas", TextureAtlas.class);
		TextureRegionDrawable moon;
		if(associatedPlanet.ability.equals(Constants.ABILITY_ATTACK_INCREASE)){
			moon = new TextureRegionDrawable(planetAtlas.findRegion("attackMoon"));

		}else if(associatedPlanet.ability.equals(Constants.ABILITY_DEFENCE_INCREASE)){
			moon = new TextureRegionDrawable(planetAtlas.findRegion("defenseMoon"));

		}else{
			moon = new TextureRegionDrawable(planetAtlas.findRegion("speedMoon"));
		}
		moon.setMinHeight(height);
		moon.setMinWidth(width);
		setDrawable(moon);
		this.associatedPlanet = associatedPlanet;
		setupMovementSpeedAndDirection();
		
	}

	private void setupMovementSpeedAndDirection() {
		
		rateOfOrbit = Math.random();
		if(rateOfOrbit < 0.4){
			rateOfOrbit = 0.5;
		}
		
		if(associatedPlanet.shipRegenRate <= 3){
			angle = 360;
			rateOfOrbit  = rateOfOrbit * -1;
		}
	}

}

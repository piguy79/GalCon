package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.model.Planet;

public class Moon extends Image{
	
	public float angle = 0;
	public double rateOfOrbit = 0;
	public Planet associatedPlanet;
	
	public Moon(AssetManager assetManager, Planet associatedPlanet, float height, float width){
		TextureAtlas planetAtlas = assetManager.get("data/images/planets.atlas", TextureAtlas.class);
		TextureRegionDrawable moon = new TextureRegionDrawable(planetAtlas.findRegion("dead_planet"));
		moon.setMinHeight(height);
		moon.setMinWidth(width);
		setDrawable(moon);
		this.associatedPlanet = associatedPlanet;
		rateOfOrbit = Math.random();
		if(rateOfOrbit < 0.4){
			rateOfOrbit = 0.5;
		}		
	}

}

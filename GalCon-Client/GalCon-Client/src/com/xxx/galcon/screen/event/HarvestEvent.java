package com.xxx.galcon.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.xxx.galcon.model.Planet;

public class HarvestEvent extends Event{
	
	private Planet planetToHarvest;

	public HarvestEvent(Planet planetToHarvest) {
		super();
		this.planetToHarvest = planetToHarvest;
	}
	
	public Planet getPlanetToHarvest() {
		return planetToHarvest;
	}

}

package com.railwaygames.solarsmash.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;

public class ClaimVictoryEvent extends Event {
	private boolean success;
	
	public ClaimVictoryEvent(boolean success){
		this.success = success;
	}
	
	public boolean isSuccess() {
		return success;
	}
}

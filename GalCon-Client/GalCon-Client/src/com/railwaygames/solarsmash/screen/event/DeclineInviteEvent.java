package com.railwaygames.solarsmash.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;

public class DeclineInviteEvent extends Event {
	private boolean success;
	
	public DeclineInviteEvent(boolean success){
		this.success = success;
	}
	
	public boolean isSuccess() {
		return success;
	}
}

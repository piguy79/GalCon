package com.xxx.galcon.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;

public class TransitionEvent extends Event {
	
	private String action;
	
	public TransitionEvent(String action) {
		this.action = action;
	}
	
	public String getAction() {
		return action;
	}

}

package com.xxx.galcon.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public class TransitionEventListener implements EventListener {

	@Override
	public boolean handle(Event event) {
		if (event instanceof TransitionEvent) {
			transition(((TransitionEvent) event).getAction());
			return true;
		}
		return false;
	}

	public void transition(String action) {
	}
}

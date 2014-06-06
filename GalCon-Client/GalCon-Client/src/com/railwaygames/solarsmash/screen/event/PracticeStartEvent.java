package com.railwaygames.solarsmash.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;

public class PracticeStartEvent extends Event {
	
	private int selectedMapKey;
	
	public PracticeStartEvent(int selectedMapKey){
		this.selectedMapKey = selectedMapKey;
	}
	
	public int getSelectedMapKey() {
		return selectedMapKey;
	}
}

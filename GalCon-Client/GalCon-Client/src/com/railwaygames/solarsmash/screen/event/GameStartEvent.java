package com.railwaygames.solarsmash.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;

public class GameStartEvent extends Event {
	
	private int selectedMapKey;
	
	public GameStartEvent(int selectedMapKey){
		this.selectedMapKey = selectedMapKey;
	}
	
	public int getSelectedMapKey() {
		return selectedMapKey;
	}

}

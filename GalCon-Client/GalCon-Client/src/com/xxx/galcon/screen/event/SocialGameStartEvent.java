package com.xxx.galcon.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;

public class SocialGameStartEvent extends Event {
	
private int selectedMapKey;
	
	public SocialGameStartEvent(int selectedMapKey){
		this.selectedMapKey = selectedMapKey;
	}
	
	public int getSelectedMapKey() {
		return selectedMapKey;
	}

}

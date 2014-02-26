package com.xxx.galcon.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public abstract class GameStartListener implements EventListener {

	@Override
	public boolean handle(Event event) {
		if(event instanceof GameStartEvent){
			startGame(((GameStartEvent) event).getSelectedMapKey());
		}else if (event instanceof SocialGameStartEvent){
			startSocialGame(((SocialGameStartEvent)event).getSelectedMapKey());
		}
		return false;
	}
	
	public void startGame(int selectedMapKey){
	}
	
	public void startSocialGame(int selectedMapKey){
		
	}

}

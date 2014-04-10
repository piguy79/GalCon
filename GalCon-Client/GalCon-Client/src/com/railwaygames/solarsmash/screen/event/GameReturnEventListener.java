package com.railwaygames.solarsmash.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public class GameReturnEventListener implements EventListener {

	@Override
	public boolean handle(Event event) {
		if(event instanceof GameReturnEvent){
			gameFound(((GameReturnEvent) event).getGameId());
		}
		return false;
	}
	
	public void gameFound(String gameId){
	}

}

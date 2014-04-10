package com.railwaygames.solarsmash.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;

public class GameReturnEvent extends Event {
	
	private String gameId;
	
	public GameReturnEvent(String gameId){
		this.gameId = gameId;
	}
	
	public String getGameId() {
		return gameId;
	}

}

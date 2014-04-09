package com.railwaygames.solarsmash.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public class ClaimVictoryEventListener implements EventListener {

	@Override
	public boolean handle(Event event) {
		if(event instanceof ClaimVictoryEvent){
			if(((ClaimVictoryEvent) event).isSuccess()){
				claimSuccess();
			}else{
				claimFailed();
			}
		}
		return false;
	}
	
	public void claimSuccess(){
	}
	
	public void claimFailed(){
	}

}

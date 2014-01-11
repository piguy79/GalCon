package com.xxx.galcon.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public abstract class DialogEventListener implements EventListener{
	
	@Override
	public boolean handle(Event event) {
		if(event instanceof CancelDialogEvent){
			cancelDialog();
		}
		return false;
	}
	
	public void cancelDialog(){};

}

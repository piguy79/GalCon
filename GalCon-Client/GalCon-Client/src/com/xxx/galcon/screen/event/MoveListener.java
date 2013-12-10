package com.xxx.galcon.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.xxx.galcon.model.Move;

public abstract class MoveListener implements EventListener{

	@Override
	public boolean handle(Event event) {
		if(event instanceof MoveEvent){
			performMove(((MoveEvent)event).getMove());
		}else if(event instanceof SendMoveEvent){
			sendMove();
		}
		return false;
	}
	
	protected void performMove(Move move){}
	
	public void sendMove(){};

}

package com.xxx.galcon.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.xxx.galcon.model.Move;

public class MoveEvent extends Event {
		
	private Move move;

	public MoveEvent(Move move) {
		super();
		this.move = move;
	}
	
	public Move getMove() {
		return move;
	}

}

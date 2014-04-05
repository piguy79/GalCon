package com.railwaygames.solarsmash.screen.event;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.railwaygames.solarsmash.model.Move;

public class SendMoveEvent extends Event {

	public List<Move> moves;

	public SendMoveEvent(List<Move> moves) {
		this.moves = moves;
	}
}

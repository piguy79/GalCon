package com.xxx.galcon.screen.event;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.xxx.galcon.model.Move;

public class SendMoveEvent extends Event {

	public List<Move> moves;

	public SendMoveEvent(List<Move> moves) {
		this.moves = moves;
	}
}

package com.xxx.galcon.screen.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.xxx.galcon.model.Move;

public class MoveEvent extends Event {

	private Move move;
	private int oldShipsToMove;

	public MoveEvent(int oldShipsToMove, Move move) {
		super();
		this.move = move;
		this.oldShipsToMove = oldShipsToMove;
	}

	public Move getMove() {
		return move;
	}

	public int getOldShipsToMove() {
		return oldShipsToMove;
	}

}

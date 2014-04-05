package com.railwaygames.solarsmash.screen.event;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.railwaygames.solarsmash.model.Move;
import com.railwaygames.solarsmash.model.Planet;

public abstract class MoveListener implements EventListener {

	@Override
	public boolean handle(Event event) {
		if (event instanceof MoveEvent) {
			MoveEvent e = (MoveEvent) event;
			performMove(e.getOldShipsToMove(), e.getMove());
		} else if (event instanceof SendMoveEvent) {
			sendMove(((SendMoveEvent) event).moves);
		} else if (event instanceof CancelDialogEvent) {
			cancelDialog();
		} else if (event instanceof HarvestEvent) {
			handleHarvest(((HarvestEvent) event).getPlanetToHarvest());
		} else if (event instanceof SliderUpdateEvent) {
			sliderUpdate(((SliderUpdateEvent) event).value);
		}
		return false;
	}

	public void sliderUpdate(int value) {

	}

	protected void performMove(int oldShipsToSend, Move move) {
	}

	public void sendMove(List<Move> moves) {
	}

	public void cancelDialog() {
	}

	public void handleHarvest(Planet planet) {
	}

}

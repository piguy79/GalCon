package com.xxx.galcon.screen.ship.selection;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;

public class ExistingMoveDialog extends MoveDialog {
	
	private Move move;

	public ExistingMoveDialog(Move move, Planet fromPlanet, Planet toPlanet, int offset,
			AssetManager assetManager, float width, float height, UISkin skin, int currentRound, Stage stage) {
		super(fromPlanet, toPlanet, offset ,fromPlanet.numberOfShips, assetManager, width, height, skin, currentRound, stage);
		this.move = move;
		shipsToSend = move.shipsToMove;
		slider.setValue(move.shipsToMove);
	}
	
}

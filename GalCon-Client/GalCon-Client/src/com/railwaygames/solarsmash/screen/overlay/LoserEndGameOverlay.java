package com.railwaygames.solarsmash.screen.overlay;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.screen.Resources;

public class LoserEndGameOverlay extends EndGameOverlay {
	

	public LoserEndGameOverlay(Resources resources, GameBoard gameBoard) {
		super(resources, gameBoard);
	}

	@Override
	String getTextForResultLabel() {
		return "Defeat.";
	}

	@Override
	String getResultFont() {
		return Constants.UI.LARGE_FONT_RED;
	}

	@Override
	String getTextForMessageLabel() {
		return "You have been overrun by enemy forces.";
	}

	@Override
	String getMessageFont() {
		return Constants.UI.DEFAULT_FONT_RED;
	}


}

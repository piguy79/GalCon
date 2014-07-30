package com.railwaygames.solarsmash.screen.overlay;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

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
		return Constants.UI.LARGE_FONT;
	}

	@Override
	String getTextForMessageLabel() {
		return "You have been overrun by enemy forces.";
	}

	@Override
	String getMessageFont() {
		return Constants.UI.DEFAULT_FONT;
	}

	@Override
	Color getResultColor() {
		return Color.RED;
	}

}

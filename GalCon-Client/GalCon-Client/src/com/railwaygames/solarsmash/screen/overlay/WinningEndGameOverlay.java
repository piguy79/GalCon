package com.railwaygames.solarsmash.screen.overlay;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class WinningEndGameOverlay extends EndGameOverlay {
	
	private ShaderLabel xpLabel;
	
	public WinningEndGameOverlay(Resources resources, GameBoard gameBoard) {
		super(resources, gameBoard);
		createResultXpLabel();
	}
	

	void createResultXpLabel() {
		int xpAwarded = Integer.parseInt(gameBoard.gameConfig.getValue(Constants.Config.XP_AWARDED_TO_WINNER));
		xpLabel = new ShaderLabel(resources.fontShader, xpAwarded + "XP", resources.skin, Constants.UI.DEFAULT_FONT_GREEN);
		xpLabel.setX((width / 2) - xpLabel.getTextBounds().width / 2);
		xpLabel.setY(resultLabel.getY() - xpLabel.getTextBounds().height * 1.2f);
		
		addActor(xpLabel);
	}


	@Override
	String getTextForResultLabel() {
		return "Victory!";
	}
	
	@Override
	String getResultFont(){
		return Constants.UI.LARGE_FONT_GREEN;
	}


	@Override
	String getTextForMessageLabel() {
		return "You have obliterated the enemy's fleet.";
	}


	@Override
	String getMessageFont() {
		return Constants.UI.DEFAULT_FONT_GREEN;
	}


}

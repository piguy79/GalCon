package com.railwaygames.solarsmash.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public abstract class EndGameOverlay extends Overlay {
	
	protected GameBoard gameBoard;
	protected ShaderLabel resultLabel;
	protected ShaderLabel messageLabel;
	protected float height;
	protected float width;

	public EndGameOverlay(Resources resources, GameBoard gameBoard) {
		super(resources);
		this.gameBoard = gameBoard;
		this.height = Gdx.graphics.getHeight();
		this.width = Gdx.graphics.getWidth();
		
		createResultLabel(getTextForResultLabel());
		createMessageLabel(getTextForMessageLabel());
	}


	protected void createResultLabel(String text){
		resultLabel = new ShaderLabel(resources.fontShader, text, resources.skin, getResultFont());
		resultLabel.setX((width / 2) - (resultLabel.getTextBounds().width / 2));
		resultLabel.setY(height * 0.75f);
		
		addActor(resultLabel);
	};
	
	private void createMessageLabel(String text) {
		messageLabel = new ShaderLabel(resources.fontShader, text, resources.skin, getMessageFont());
		messageLabel.setX((width / 2) - (messageLabel.getTextBounds().width / 2));
		messageLabel.setY(resultLabel.getY() - (height * 0.15f));
		
		addActor(messageLabel);
	}
		
	abstract String getTextForResultLabel();
	
	abstract String getTextForMessageLabel();

	
	abstract String getResultFont();
	
	abstract String getMessageFont();

}

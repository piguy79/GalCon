package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.xxx.galcon.Constants;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.screen.GraphicsUtils;

public class CountLabel extends Group {
	
	private int count;
	private ImageButton background;

	private ShaderLabel countLabel;

	
	private ShaderProgram fontShader;
	private UISkin skin;
	
	public CountLabel(int count, ShaderProgram fontShader, UISkin skin){
		this.count = count;
		this.fontShader = fontShader;
		this.skin = skin;
		
		createBg();
		createCountLabel();
	}

	private void createBg() {
		background = new ImageButton(skin, Constants.UI.COUNT_LABEL);
		background.setSize(GraphicsUtils.actionButtonSize * 0.4f, GraphicsUtils.actionButtonSize * 0.4f);
		
		addActor(background);
		
	}

	private void createCountLabel() {
		countLabel = new ShaderLabel(fontShader, "" + count, skin, Constants.UI.X_SMALL_FONT);
		countLabel.setX((background.getX() + (background.getWidth() / 2)) - (countLabel.getTextBounds().width / 2));
		countLabel.setY(background.getY() + (background.getHeight() / 2) - (countLabel.getTextBounds().height * 0.7f));
		
		addActor(countLabel);
	}

}

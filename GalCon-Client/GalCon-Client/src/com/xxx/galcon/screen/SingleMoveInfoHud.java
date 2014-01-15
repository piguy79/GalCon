package com.xxx.galcon.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class SingleMoveInfoHud extends Group {
	
	private Move move;
	private AssetManager assetManager;
	private ShaderProgram fontShader;
	private UISkin skin;
	
	private AtlasRegion bgRegion;
	
	public SingleMoveInfoHud(Move move, AssetManager assetManager, ShaderProgram fontShader, UISkin skin, float width, float height){
		this.move = move;
		this.assetManager = assetManager;
		this.fontShader = fontShader;
		this.skin = skin;
		setHeight(height);
		setWidth(width);
		
		createBackground();
		createLabels();
	}
	
	
	private void createLabels() {
		ShaderLabel numberOfShips = new ShaderLabel(fontShader, "Sending " + move.shipsToMove, skin, Constants.UI.DEFAULT_FONT);
		numberOfShips.setX(getX());
		addActor(numberOfShips);
		
		ShaderLabel duration = new ShaderLabel(fontShader, "Duration " + Math.round(move.duration), skin, Constants.UI.DEFAULT_FONT);
		duration.setX(getWidth() - duration.getTextBounds().width);
		duration.setY((float) (getHeight() - (duration.getTextBounds().height * 1.5)));
		addActor(duration);
		
	}


	private void createBackground() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		bgRegion = gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}

}

package com.xxx.galcon.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class MoveButton extends Group implements Comparable<MoveButton> {
	
	private static final Color NEW_MOVE = Color.valueOf("E8920C");
	private AssetManager assetManager;
	private AtlasRegion bgTexture;
	private Move move;
	private UISkin skin;
	private ShaderProgram fontShader;
	private GameBoard gameBoard;
	
	public MoveButton(AssetManager assetManager,GameBoard gameBoard, Move move, UISkin skin, ShaderProgram fontShader,  float width, float height) {
		super();
		this.assetManager = assetManager;
		this.move = move;
		this.skin = skin;
		this.fontShader = fontShader;
		this.gameBoard = gameBoard;
		setWidth(width);
		setHeight(height);
		
		createLayout();
	}


	private void createLayout() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		bgTexture = gameBoardAtlas.findRegion("bottom_bar_ship_button");
		createBackground();
		
		addLabels();
	}
	
	public boolean isActive(){
		return move.startingRound == gameBoard.roundInformation.currentRound && !GameLoop.USER.hasMoved(gameBoard);
	}



	private void addLabels() {
		float padding = getWidth() * 0.1f;
		
		ShaderLabel duration = new ShaderLabel(fontShader, "" + Math.round(move.duration), skin, Constants.UI.DEFAULT_FONT_BLACK);
		duration.setX(getWidth() - (duration.getTextBounds().width + padding));
		duration.setY(getHeight() - (duration.getTextBounds().height + padding));
		
		ShaderLabel fleet = new ShaderLabel(fontShader, "" + move.shipsToMove, skin, Constants.UI.DEFAULT_FONT_BLACK);
		fleet.setX(0);
		fleet.setY(0);
		
		addActor(duration);
		addActor(fleet);
		
	}
	
	private void createBackground() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		bgTexture = gameBoardAtlas.findRegion("bottom_bar_ship_button");
		Image backGround = new Image(new TextureRegionDrawable(bgTexture));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		
		if(isActive()){
			backGround.addAction(Actions.color(NEW_MOVE, 0.4f));
		}
		
		addActor(backGround);
	}
	
	public Move getMove() {
		return move;
	}


	@Override
	public int compareTo(MoveButton otherMove) {
		if(this.move.startingRound < otherMove.getMove().startingRound){
			return -1;
		}else if(this.move.startingRound > otherMove.getMove().startingRound){
			return 1;
		}
		return 0;
	}

}

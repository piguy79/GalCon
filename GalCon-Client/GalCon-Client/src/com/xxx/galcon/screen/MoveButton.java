package com.xxx.galcon.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class MoveButton extends Table {
	
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
		setFillParent(false);
		setWidth(width);
		setHeight(height);
		
		createLayout();
	}


	private void createLayout() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		bgTexture = gameBoardAtlas.findRegion("bottom_bar_ship_button");
		setBackground(new TextureRegionDrawable(bgTexture));
		if(isActive()){
			addAction(Actions.color(NEW_MOVE, 0.4f));
		}
		addLabels();
	}
	
	public boolean isActive(){
		return move.startingRound == gameBoard.roundInformation.currentRound && !GameLoop.USER.hasMoved(gameBoard);
	}



	private void addLabels() {
		ShaderLabel duration = new ShaderLabel(fontShader, "" + Math.round(move.duration), skin, Constants.UI.DEFAULT_FONT_BLACK);
		ShaderLabel fleet = new ShaderLabel(fontShader, "" + move.shipsToMove, skin, Constants.UI.DEFAULT_FONT_BLACK);
		
		add(fleet).expandX().right().height(getHeight() * 0.3f);
		row().height(getHeight() * 0.4f);
		add(duration).expandX().left().height(getHeight() * 0.3f);
		
	}

}

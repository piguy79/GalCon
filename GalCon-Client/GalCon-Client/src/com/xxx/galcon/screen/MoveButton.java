package com.xxx.galcon.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class MoveButton extends Table {
	
	private AssetManager assetManager;
	private AtlasRegion bgTexture;
	private Move move;
	private UISkin skin;
	private ShaderProgram fontShader;
	
	public MoveButton(AssetManager assetManager, Move move, UISkin skin, ShaderProgram fontShader,  float width, float height) {
		super();
		this.assetManager = assetManager;
		this.move = move;
		this.skin = skin;
		this.fontShader = fontShader;
		setFillParent(false);
		setWidth(width);
		setHeight(height);
		
		createLayout();
	}


	private void createLayout() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		bgTexture = gameBoardAtlas.findRegion("bottom_bar_ship_button");
		setBackground(new TextureRegionDrawable(bgTexture));
		addLabels();
	}



	private void addLabels() {
		ShaderLabel duration = new ShaderLabel(fontShader, "" + Math.round(move.duration), skin, Constants.UI.DEFAULT_FONT_BLACK);
		ShaderLabel fleet = new ShaderLabel(fontShader, "" + move.shipsToMove, skin, Constants.UI.DEFAULT_FONT_BLACK);
		
		add(fleet).expandX().right();
		row().height(getHeight() * 0.4f);
		add(duration).expandX().left().padBottom(getHeight() * 0.1f);
		
	}

}

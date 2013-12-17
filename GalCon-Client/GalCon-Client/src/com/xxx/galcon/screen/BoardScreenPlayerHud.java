package com.xxx.galcon.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.esotericsoftware.tablelayout.Cell;
import com.xxx.galcon.Constants;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.widget.ActionButton;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class BoardScreenPlayerHud extends Table {
	
	private AtlasRegion bgTexture;
	private AssetManager assetManager;
	private UISkin skin;
	private ShaderProgram fontShader;
	
	public BoardScreenPlayerHud(AssetManager assetManager, UISkin skin, ShaderProgram fontShader, float width, float height, Point position){
		this.assetManager = assetManager;
		this.skin = skin;
		this.fontShader = fontShader;
		setWidth(width);
		setHeight(height);
		setPosition(position.x, position.y);
		createTable();
		createLayout();
	}

	private void createLayout() {
		left();
		Actor backButton = createBackButton();
		Actor slashLine = createSlash();
		Actor userTable = createUserTable();
		
		
		addActorAtSize(backButton).padLeft(5);
		addActorAtSize(slashLine);
		add(userTable).width(getWidth() * 0.6f).height(getHeight());

	}
	
	private Actor createUserTable() {
		Table userTable = new Table();
		userTable.center();
		userTable.setHeight(getHeight());
		userTable.add(new ShaderLabel(fontShader, "Conor", skin, Constants.UI.DEFAULT_FONT));
		userTable.row().height(0).width(0);
		userTable.add(new ShaderLabel(fontShader, "vs", skin, Constants.UI.DEFAULT_FONT)).center();
		userTable.row();
		userTable.add(new ShaderLabel(fontShader, "Conor", skin, Constants.UI.DEFAULT_FONT)).padBottom(5);

		
		return userTable;
	}

	private Cell<Actor> addActorAtSize(Actor actor){
		return add(actor).width(actor.getWidth()).height(actor.getHeight());
	}

	private Actor createSlash() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		TextureRegionDrawable trd = new TextureRegionDrawable(gameBoardAtlas.findRegion("slash_line"));
		ImageButtonStyle style = new ImageButtonStyle(trd, trd, trd, trd, trd, trd);
		ImageButton imagebutton = new ImageButton(style);
		imagebutton.setHeight(getHeight());
		imagebutton.setWidth(getWidth() * 0.1f);
		
		
		return imagebutton;
		
	}

	private Actor createBackButton() {
		float buttonSize = getHeight() * 0.8f;
		ActionButton backButton = new ActionButton(skin, "backButton", buttonSize, buttonSize);
		return backButton;
	}

	private void createTable() {
		TextureAtlas menuAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		bgTexture = menuAtlas.findRegion("bg_dark_gray_10x10");
		setBackground(new TextureRegionDrawable(bgTexture));
	}

}

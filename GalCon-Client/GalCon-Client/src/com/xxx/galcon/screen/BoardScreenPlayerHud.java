package com.xxx.galcon.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.esotericsoftware.tablelayout.Cell;
import com.xxx.galcon.Constants;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.event.TransitionEvent;
import com.xxx.galcon.screen.widget.ActionButton;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class BoardScreenPlayerHud extends Table {
	
	private AtlasRegion bgTexture;
	private AssetManager assetManager;
	private UISkin skin;
	private ShaderProgram fontShader;
	private GameBoard gameBoard;
	
	public BoardScreenPlayerHud(AssetManager assetManager, UISkin skin, ShaderProgram fontShader, float width, float height, Point position, GameBoard gameBoard){
		this.assetManager = assetManager;
		this.skin = skin;
		this.fontShader = fontShader;
		this.gameBoard = gameBoard;
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
		Actor refreshButton = createRefreshButton();
		
		
		addActorAtSize(backButton).padLeft(5);
		addActorAtSize(slashLine);
		add(userTable).width(getWidth() * 0.6f).height(getHeight());
		addActorAtSize(refreshButton);

	}
	
	

	private Actor createUserTable() {
		Table userTable = new Table();
		userTable.center();
		userTable.setHeight(getHeight());
		if(!gameBoard.players.get(0).hasMoved(gameBoard)){
			// Draw the line beside the name to indicate this player can move
		}
		userTable.add(new ShaderLabel(fontShader, playerInfo(gameBoard.players.get(0)), skin, Constants.UI.SMALL_FONT));
		userTable.row().height(0).width(0);
		userTable.add(new ShaderLabel(fontShader, "vs", skin, Constants.UI.SMALL_FONT)).center();
		userTable.row();
		if(gameBoard.players.size() > 1 && !gameBoard.players.get(1).hasMoved(gameBoard)){
			// Draw the line for the second player indicating they can move again.
		}
		userTable.add(new ShaderLabel(fontShader, gameBoard.players.size() > 1 ? playerInfo(gameBoard.players.get(1)) : "Awaiting opponent", skin, Constants.UI.SMALL_FONT)).padBottom(5);

		
		return userTable;
	}
	
	private String playerInfo(Player player){
		return player.handle + "(" + player.rank.level + ")";
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
		imagebutton.setWidth(getWidth() * 0.08f);
		
		
		return imagebutton;
	}

	private Actor createBackButton() {
		float buttonSize = getHeight() * 0.8f;
		ActionButton backButton = new ActionButton(skin, "backButton", buttonSize, buttonSize);
		backButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent clickEvent, float x, float y) {
				TransitionEvent event = new TransitionEvent(Action.BACK);
				fire(event);
			}
		});
		return backButton;
	}
	
	private Actor createRefreshButton() {
		float buttonSize = getHeight() * 0.8f;
		ActionButton refreshButton = new ActionButton(skin, "refreshButton", buttonSize, buttonSize);
		refreshButton.addListener(new ClickListener(){@Override
		public void clicked(InputEvent event, float x, float y) {
			TransitionEvent transitionEvent = new TransitionEvent(Action.REFRESH);
			fire(transitionEvent);
		}});
		
		return refreshButton;
	}

	private void createTable() {
		TextureAtlas menuAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		bgTexture = menuAtlas.findRegion("bg_dark_gray_10x10");
		setBackground(new TextureRegionDrawable(bgTexture));
	}

}

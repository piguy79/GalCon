package com.xxx.galcon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
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
import com.xxx.galcon.screen.widget.Line;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class BoardScreenPlayerHud extends Group {
	
	private AtlasRegion bgTexture;
	private AssetManager assetManager;
	private UISkin skin;
	private ShaderProgram fontShader;
	private GameBoard gameBoard;
	private TextureAtlas gameBoardAtlas;
	
	private ActionButton backButton;
	private Image firstSlash;
	private Image secondSlash;
	private ShaderLabel firstPlayer;
	private ShaderLabel vs;
	private ShaderLabel secondPlayer;
	
	public BoardScreenPlayerHud(AssetManager assetManager, UISkin skin, ShaderProgram fontShader, float width, float height, Point position, GameBoard gameBoard){
		this.assetManager = assetManager;
		this.skin = skin;
		this.fontShader = fontShader;
		this.gameBoard = gameBoard;
		this.gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);

		setWidth(Gdx.graphics.getWidth());
		setHeight(height);
		setPosition(position.x, position.y);
		createTable();
		createLayout();
	}

	private void createLayout() {
		createBackButton();
		createFirstSlash();
		createSecondSlash();
		createUserTable();
		createRefreshButton();

	}
	
	private void createUserTable(){
		firstPlayer = new ShaderLabel(fontShader, playerInfo(gameBoard.players.get(0)), skin, Constants.UI.SMALL_FONT);
		firstPlayer.setWidth(getWidth() * 0.5f);
		firstPlayer.setX(secondSlash.getX() + getWidth() * 0.1f);
		firstPlayer.setY((getHeight() - firstPlayer.getTextBounds().height) - (getHeight() * 0.2f));
		firstPlayer.setAlignment(Align.center);
		addActor(firstPlayer);
		
		vs = new ShaderLabel(fontShader, "vs", skin, Constants.UI.SMALL_FONT);
		vs.setWidth(getWidth() * 0.5f);
		vs.setX(secondSlash.getX() + getWidth() * 0.1f);
		vs.setY((firstPlayer.getY() - vs.getTextBounds().height) - getHeight() * 0.1f);
		vs.setAlignment(Align.center);
		addActor(vs);
		
		secondPlayer = new ShaderLabel(fontShader, gameBoard.players.size() > 1 ? playerInfo(gameBoard.players.get(1)) : "[waiting for opponent]", skin, Constants.UI.SMALL_FONT);
		secondPlayer.setWidth(getWidth() * 0.5f);
		secondPlayer.setX(secondSlash.getX() + getWidth() * 0.1f);
		secondPlayer.setY((vs.getY() - secondPlayer.getTextBounds().height) - getHeight() * 0.1f);
		secondPlayer.setAlignment(Align.center);
		addActor(secondPlayer);
		
		addPlayerLine(gameBoard.players.get(0), firstPlayer);
		if(gameBoard.players.size() > 1){
			addPlayerLine(gameBoard.players.get(1), secondPlayer);
		}
		
		
	}

	private void addPlayerLine(Player player, ShaderLabel referencePoint) {
		if(!player.hasMoved(gameBoard)){
			Actor line = line();
			line.setX((referencePoint.getX() + ((referencePoint.getWidth() * 0.5f) - (referencePoint.getTextBounds().width * 0.5f))) - line.getWidth());
			line.setY(referencePoint.getY() + (referencePoint.getTextBounds().height * 0.5f));
			addActor(line);
		}
	}
	
	private Actor line(){
		TextureRegion lineRegion = gameBoardAtlas.findRegion("line");
		Line line = new Line(Color.valueOf("E8920C"), getWidth() * 0.05f, lineRegion);
		line.setHeight(getHeight() * 0.1f);
		
		return line;
	}
	
	private String playerInfo(Player player){
		return player.handle + "(" + player.rank.level + ")";
	}


	private void createFirstSlash() {
		firstSlash = createSlash();
		firstSlash.setX(backButton.getX() + backButton.getWidth() + (firstSlash.getWidth() * 0.3f));
		
		
		addActor(firstSlash);
	}
	
	private void createSecondSlash() {
		secondSlash = createSlash();
		secondSlash.setX(firstSlash.getX() + (firstSlash.getWidth() * 0.4f));
		
		addActor(secondSlash);
	}

	private Image createSlash() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		TextureRegionDrawable trd = new TextureRegionDrawable(gameBoardAtlas.findRegion("slash_line"));
		Image slash = new Image(trd);
		slash.setWidth(getWidth() * 0.1f);
		return slash;
	}
	
	private void createBackButton() {
		float buttonSize = getHeight() * 0.8f;
		backButton = new ActionButton(skin, "backButton", buttonSize, buttonSize);
		backButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent clickEvent, float x, float y) {
				TransitionEvent event = new TransitionEvent(Action.BACK);
				fire(event);
			}
		});
		backButton.setY(getHeight() * 0.1f);
		backButton.setX(backButton.getWidth() * 0.05f);
		addActor(backButton);
	}
	
	private void createRefreshButton() {
		float buttonSize = getHeight() * 0.8f;
		ActionButton refreshButton = new ActionButton(skin, "refreshButton", buttonSize, buttonSize);
		refreshButton.addListener(new ClickListener(){@Override
		public void clicked(InputEvent event, float x, float y) {
			TransitionEvent transitionEvent = new TransitionEvent(Action.REFRESH);
			fire(transitionEvent);
		}});
		refreshButton.setX((firstPlayer.getX() + firstPlayer.getWidth()) + getWidth() * 0.01f);
		refreshButton.setY(refreshButton.getY() + getHeight() * 0.1f);
		addActor(refreshButton);
		
	}

	private void createTable() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		bgTexture = gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgTexture));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}

}

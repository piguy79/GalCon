package com.xxx.galcon.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.event.MoveEvent;
import com.xxx.galcon.screen.event.SendMoveEvent;
import com.xxx.galcon.screen.widget.ActionButton;

public class MoveHud extends Table {
	
	private AssetManager assetManager;
	private UISkin skin;
	private ShaderProgram fontShader;
	private AtlasRegion bgTexture;
	private Map<Move, MoveButton> moves;
	private Table moveButtonHolder;
	private ScrollPane scrollPane;
	private Table innerContainer;

	public MoveHud(AssetManager assetManager, UISkin skin,ShaderProgram fontShader, float width, float height) {
		super();
		this.assetManager = assetManager;
		this.skin = skin;
		this.moves = new HashMap<Move, MoveButton>();
		this.fontShader = fontShader;
		setWidth(width);
		setHeight(height);
		createTable();
		addPerformMoveButton();
		addMoveButtons();
	}

	private void createTable() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		bgTexture = gameBoardAtlas.findRegion("bottom_bar");
		setBackground(new TextureRegionDrawable(bgTexture));
	}
	
	private void addPerformMoveButton() {
		ActionButton performMove =  new ActionButton(skin,"performMoveButton", getWidth() * 0.12f, getWidth() * 0.12f, new Point(getX() + (getWidth() * 0.83f), getY() + (getHeight() * 0.05f)));
		performMove.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new SendMoveEvent());
			}
		});
		addActor(performMove);
	}
	


	private void addMoveButtons() {
		moveButtonHolder = new Table();
		moveButtonHolder.setWidth(getWidth() * 0.8f);
		moveButtonHolder.setHeight(getHeight() * 0.9f);
		moveButtonHolder.setX(0);
		moveButtonHolder.setY(getY() - (getHeight() * 0.53f));
		
		innerContainer = new Table();
		scrollPane = new ScrollPane(innerContainer);
		scrollPane.setScrollingDisabled(false, true);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setWidth(moveButtonHolder.getWidth());
		
		moveButtonHolder.addActor(scrollPane);
		
		innerContainer.defaults().padLeft(5).padRight(5).left().width(moveButtonHolder.getWidth() * 0.15f)
		.height(moveButtonHolder.getHeight() * 0.9f);
		
		
		addActor(moveButtonHolder);
		
		for(Move move : moves.keySet()){
			addMoveToMap(move);
		}
		
		renderMoves();
	}

	private void addMoveToMap(final Move move) {
		if(moves.get(move) == null){
			float buttonWidth = moveButtonHolder.getWidth() * 0.15f;
			MoveButton button = new MoveButton(assetManager, move,skin, fontShader, buttonWidth, moveButtonHolder.getHeight() * 0.9f);
			
			button.addListener(new ClickListener(){@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new MoveEvent(move));
			}});
			moves.put(move, button);
		}
	}
	
	public void addMove(Move move){
		if(move.playerHandle.equals(GameLoop.USER.handle)){
			addMoveToMap(move);
			renderMoves();
		}
	}
	
	public void addMoves(List<Move> moves){
		for(Move move : moves){
			addMove(move);
		}
	}
	
	public void renderMoves(){
		innerContainer.clearChildren();
		for(MoveButton button : moves.values()){
			innerContainer.add(button);
		}
	}

}

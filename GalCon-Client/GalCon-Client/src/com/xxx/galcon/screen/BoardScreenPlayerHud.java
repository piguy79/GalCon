package com.xxx.galcon.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.event.TransitionEvent;
import com.xxx.galcon.screen.widget.ActionButton;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class BoardScreenPlayerHud extends Group {

	private AtlasRegion bgTexture;
	private AssetManager assetManager;
	private UISkin skin;
	private ShaderProgram fontShader;
	private GameBoard gameBoard;

	private ActionButton backButton;
	private Image firstSlash;
	private Image secondSlash;
	private ShaderLabel firstPlayer;
	private ShaderLabel vs;
	private ShaderLabel secondPlayer;
	
	private final Map<String, String> ABILITY_TO_ABBREVIATION = new HashMap<String, String>() {
		{
			put(Constants.ABILITY_ATTACK_INCREASE, "A");
			put(Constants.ABILITY_DEFENCE_INCREASE, "D");
			put(Constants.ABILITY_SPEED, "S");
			put(Constants.ABILITY_REGEN_BLOCK, "B");
		}
	};

	public BoardScreenPlayerHud(AssetManager assetManager, UISkin skin, ShaderProgram fontShader, float width,
			float height, Point position, GameBoard gameBoard) {
		this.assetManager = assetManager;
		this.skin = skin;
		this.fontShader = fontShader;
		this.gameBoard = gameBoard;

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
		// createRefreshButton();
		createOptionsButton();
	}

	private void createUserTable() {
		firstPlayer = new ShaderLabel(fontShader, playerInfo(gameBoard.players.get(0)), skin, findFontStyleForPlayer(0));
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

		secondPlayer = new ShaderLabel(fontShader, gameBoard.players.size() > 1 ? playerInfo(gameBoard.players.get(1))
				: "[waiting for opponent]", skin, gameBoard.players.size() > 1 ? findFontStyleForPlayer(1)
				: Constants.UI.SMALL_FONT_GREEN);
		secondPlayer.setWidth(getWidth() * 0.5f);
		secondPlayer.setX(secondSlash.getX() + getWidth() * 0.1f);
		secondPlayer.setY((vs.getY() - secondPlayer.getTextBounds().height) - getHeight() * 0.1f);
		secondPlayer.setAlignment(Align.center);
		addActor(secondPlayer);

	}

	private String findFontStyleForPlayer(int index) {
		String playerFontStyle = Constants.UI.SMALL_FONT;
		if (gameBoard.players.size() > index && !gameBoard.players.get(index).hasMoved(gameBoard)) {
			playerFontStyle = Constants.UI.SMALL_FONT_GREEN;
		}
		return playerFontStyle;
	}

	private String playerInfo(Player player) {
		
		String playerInfo = player.handle + "[" + player.rank.level + "]";
		if (!player.hasMoved(gameBoard)) {
			playerInfo = "--" +  playerInfo + "--";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(playerInfo);
		sb.append("  ");
		sb.append(abilitiesToString(gameBoard.ownedPlanetAbilities(player)));
		
		return sb.toString();
	}

	private void createFirstSlash() {
		firstSlash = createSlash();
		firstSlash.setX(backButton.getX() + backButton.getWidth() + (firstSlash.getWidth() * 0.3f));

		addActor(firstSlash);
	}

	private void createSecondSlash() {
		secondSlash = createSlash();
		secondSlash.setX(firstSlash.getX() + (firstSlash.getWidth() * 0.28f));

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
		backButton = new ActionButton(skin, "backButton", new Point(10, getHeight() * 0.1f));
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent clickEvent, float x, float y) {
				TransitionEvent event = new TransitionEvent(Action.BACK);
				fire(event);
			}
		});
		addActor(backButton);
	}

	private void createOptionsButton() {
		ImageButton button = new ImageButton(skin, "optionsButton");
		float ratio = button.getWidth() / button.getHeight();
		float height = getHeight() * 0.6f;
		float width = ratio * height;
		button.setColor(Color.BLUE);
		button.setBounds(getWidth() - width - 0.07f * getWidth(), getHeight() * 0.2f, width, height);
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				TransitionEvent transitionEvent = new TransitionEvent(Action.OPTIONS);
				fire(transitionEvent);
			}
		});
		addActor(button);
	}

	private void createTable() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		bgTexture = gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgTexture));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}
	
	

	private Map<String, Integer> bonuses = new HashMap<String, Integer>();

	private String abilitiesToString(List<String> planetAbilities) {
		if (planetAbilities.isEmpty()) {
			return "";
		}
		bonuses.clear();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < planetAbilities.size(); ++i) {
			String ability = planetAbilities.get(i);
			String abbrev = ABILITY_TO_ABBREVIATION.get(ability);
			if (abbrev == null) {
				throw new IllegalArgumentException("PlayerInfoHud does not understand: " + planetAbilities.get(i));
			}
			Integer configBonus = Integer.valueOf(gameBoard.gameConfig.getValue(ability));
			Integer bonus = 0;
			if (bonuses.containsKey(abbrev)) {
				bonus = bonuses.get(abbrev);
			}
			bonus += configBonus;
			bonuses.put(abbrev, bonus);
		}

		for (Map.Entry<String, Integer> values : bonuses.entrySet()) {
			sb.append("+").append(values.getValue()).append("%").append(values.getKey()).append(" ");
		}

		return sb.toString();
	}
}

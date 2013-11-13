package com.xxx.galcon.screen.hud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.xxx.galcon.Constants;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Player;

public class HeaderHud extends Hud {
	public static final float HEADER_HEIGHT_RATIO = 0.1f;

	private GameBoard gameBoard;
	private Button backButton;
	private Button refreshButton;
	private AtlasRegion darkGrayBg10x10;
	private AtlasRegion slashLine;
	private AtlasRegion arrowSolidLine;

	private AssetManager assetManager;

	private final Map<String, String> ABILITY_TO_ABBREVIATION = new HashMap<String, String>() {
		{
			put(Constants.ABILITY_ATTACK_INCREASE, "A");
			put(Constants.ABILITY_DEFENCE_INCREASE, "D");
			put(Constants.ABILITY_SPEED, "S");
			put(Constants.ABILITY_REGEN_BLOCK, "B");
		}
	};

	public void setGameBoard(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}

	public HeaderHud(AssetManager assetManager) {
		super();

		TextureAtlas menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);

		backButton = new BackHudButton(menusAtlas);
		refreshButton = new RefreshHudButton(menusAtlas);
		addHudButton(backButton);
		addHudButton(refreshButton);

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		darkGrayBg10x10 = menusAtlas.findRegion("bg_dark_gray_10x10");
		slashLine = gameBoardAtlas.findRegion("slash_line");
		arrowSolidLine = menusAtlas.findRegion("arrow_solid_line");

		this.assetManager = assetManager;
	}

	public void associateCurrentRoundInformation(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}

	@Override
	public void doRender(float delta, SpriteBatch spriteBatch) {

		int height = (int) (Gdx.graphics.getHeight() * .1f);
		spriteBatch.draw(darkGrayBg10x10, 0, Gdx.graphics.getHeight() - height, Gdx.graphics.getWidth(), height);
		spriteBatch.draw(slashLine, Gdx.graphics.getWidth() * .18f, Gdx.graphics.getHeight() - height,
				Gdx.graphics.getWidth() * .1f, height);
		spriteBatch.draw(slashLine, Gdx.graphics.getWidth() * .21f, Gdx.graphics.getHeight() - height,
				Gdx.graphics.getWidth() * .1f, height);

		BitmapFont font = Fonts.getInstance(assetManager).mediumFont();

		Player player1 = gameBoard.players.get(0);
		String info1 = getPlayerInfoText(player1);

		Player player2 = gameBoard.players.size() == 2 ? gameBoard.players.get(1) : null;
		String info2 = getPlayerInfoText(player2);

		int fontHalfWidth = (int) (font.getBounds(info1).width) / 2;
		int fontHeight = (int) (font.getBounds(info1).height);
		int y = (int) (Gdx.graphics.getHeight() * .985f);
		int ySpacing = (int) (Gdx.graphics.getHeight() * 0.028f);
		int xTextMidPoint = Gdx.graphics.getWidth() / 2 + (int) (Gdx.graphics.getWidth() * .125f);
		font.setColor(1.0f, 0.9f, 0.0f, 1.0f);
		font.draw(spriteBatch, info1, xTextMidPoint - fontHalfWidth, y);

		renderPlayersTurn(player1, xTextMidPoint - fontHalfWidth, y - fontHeight, fontHalfWidth * 2, spriteBatch);

		String vs = "vs";
		fontHalfWidth = (int) (font.getBounds(vs).width) / 2;
		y -= ySpacing;
		font.setColor(1.0f, 0.9f, 0.0f, 1.0f);
		font.draw(spriteBatch, vs, xTextMidPoint - fontHalfWidth, y);

		fontHalfWidth = (int) (font.getBounds(info2).width) / 2;
		y -= ySpacing;
		font.setColor(1.0f, 0.9f, 0.0f, 1.0f);
		font.draw(spriteBatch, info2, xTextMidPoint - fontHalfWidth, y);

		renderPlayersTurn(player2, xTextMidPoint - fontHalfWidth, y - fontHeight, fontHalfWidth * 2, spriteBatch);

		font.setColor(Color.WHITE);

		refreshButton.setEnabled(true);

		if (gameBoard.wasADraw() || gameBoard.hasWinner()) {
			refreshButton.setEnabled(false);
		}
	}

	private void renderPlayersTurn(Player player, int x, int y, int width, SpriteBatch spriteBatch) {
		if (player == null || !player.hasMoved(gameBoard) || isUnspecifiedPlayersTurn(player)) {
			int arrowWidth = (int) (Gdx.graphics.getWidth() * .04f);
			int arrowBarHeight = (int) (arrowWidth * 0.35f);
			spriteBatch.draw(arrowSolidLine, x - arrowWidth - 5, y + 2, arrowWidth, arrowBarHeight);
			spriteBatch.draw(arrowSolidLine, x + width + 5, y + 2, arrowWidth, arrowBarHeight);
		}
	}

	private boolean isUnspecifiedPlayersTurn(Player player) {
		if (player == null) {
			if (!GameLoop.USER.hasMoved(gameBoard)) {
				return true;
			}
		}

		return false;
	}

	private String getPlayerInfoText(Player player) {
		if (player == null) {
			return "Waiting for opponent";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(player.handle + " [" + player.rank.level + "] ");
		sb.append(abilitiesToString(gameBoard.ownedPlanetAbilities(player)));

		return sb.toString();
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

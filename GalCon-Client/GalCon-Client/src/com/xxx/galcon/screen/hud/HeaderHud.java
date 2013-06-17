package com.xxx.galcon.screen.hud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.Constants;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Player;

public class HeaderHud extends Hud {
	private GameBoard gameBoard;
	private HudButton backButton;
	private Texture darkGrayBg10x10;
	private Texture slashLine;

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

		backButton = new BackHudButton(assetManager);
		addHudButton(backButton);

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		darkGrayBg10x10 = assetManager.get("data/images/bg_dark_gray_10x10.png", Texture.class);
		slashLine = assetManager.get("data/images/slash_line.png", Texture.class);
	}

	@Override
	public void doRender(float delta, SpriteBatch spriteBatch) {

		int height = (int) (Gdx.graphics.getHeight() * .1f);
		spriteBatch.draw(darkGrayBg10x10, 0, Gdx.graphics.getHeight() - height, Gdx.graphics.getWidth(), height);
		spriteBatch.draw(slashLine, Gdx.graphics.getWidth() * .18f, Gdx.graphics.getHeight() - height,
				Gdx.graphics.getWidth() * .1f, height);
		spriteBatch.draw(slashLine, Gdx.graphics.getWidth() * .21f, Gdx.graphics.getHeight() - height,
				Gdx.graphics.getWidth() * .1f, height);

		BitmapFont font = Fonts.getInstance().mediumFont();

		Player player = gameBoard.players.get(0);
		String info1 = getPlayerInfoText(player);

		player = gameBoard.players.size() == 2 ? gameBoard.players.get(1) : null;
		String info2 = getPlayerInfoText(player);

		int fontHalfWidth = (int) (font.getBounds(info1).width) / 2;
		int y = (int) (Gdx.graphics.getHeight() * .985f);
		int ySpacing = (int) (Gdx.graphics.getHeight() * 0.028f);
		int xTextMidPoint = Gdx.graphics.getWidth() / 2 + (int) (Gdx.graphics.getWidth() * .125f);
		font.setColor(1.0f, 0.9f, 0.0f, 1.0f);
		font.draw(spriteBatch, info1, xTextMidPoint - fontHalfWidth, y);

		String vs = "vs";
		fontHalfWidth = (int) (font.getBounds(vs).width) / 2;
		y -= ySpacing;
		font.setColor(1.0f, 0.9f, 0.0f, 1.0f);
		font.draw(spriteBatch, vs, xTextMidPoint - fontHalfWidth, y);

		fontHalfWidth = (int) (font.getBounds(info2).width) / 2;
		y -= ySpacing;
		font.setColor(1.0f, 0.9f, 0.0f, 1.0f);
		font.draw(spriteBatch, info2, xTextMidPoint - fontHalfWidth, y);

		font.setColor(Color.WHITE);
	}

	private String getPlayerInfoText(Player player) {
		if (player == null) {
			return "--Waiting for player--";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(player.handle + " [" + player.rank.level + "]");
		sb.append(abilitiesToString(gameBoard.ownedPlanetAbilities(player)));

		return sb.toString();
	}

	private String abilitiesToString(List<String> planetAbilities) {
		if (planetAbilities.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < planetAbilities.size(); ++i) {
			String abbrev = ABILITY_TO_ABBREVIATION.get(planetAbilities.get(i));
			if (abbrev == null) {
				throw new IllegalArgumentException("PlayerInfoHud does not understand: " + planetAbilities.get(i));
			}
			sb.append("+").append(abbrev).append(" ");
		}

		return sb.toString();
	}
}

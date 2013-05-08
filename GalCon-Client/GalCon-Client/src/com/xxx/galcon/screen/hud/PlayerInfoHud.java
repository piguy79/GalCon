package com.xxx.galcon.screen.hud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.Constants;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Player;

public class PlayerInfoHud extends Hud {
	private GameBoard gameBoard;

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

	@Override
	public void doRender(float delta, SpriteBatch spriteBatch) {
		BitmapFont font = Fonts.getInstance().mediumFont();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < gameBoard.players.size(); ++i) {
			Player player = gameBoard.players.get(i);

			if (i > 0) {
				sb.append("     vs.      ");
			}

			sb.append(player.handle + " (Lvl " + player.rank.level + ") ");
			sb.append(abilitiesToString(gameBoard.ownedPlanetAbilities(player)));
		}

		String info = sb.toString();
		int fontHalfWidth = (int) (font.getBounds(info).width) / 2;

		int y = (int) (Gdx.graphics.getHeight() * .98f);
		int width = Gdx.graphics.getWidth() / 2;
		font.draw(spriteBatch, info, width - fontHalfWidth, y);
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

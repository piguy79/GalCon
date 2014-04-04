package com.xxx.galcon.screen.hud;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class RoundInformationTopHud extends Group {
	private Resources resources;
	private GameBoard gameBoard;
	private AtlasRegion bgRegion;

	public RoundInformationTopHud(GameBoard gameBoard, Resources resources, float width, float height) {
		this.resources = resources;
		this.gameBoard = gameBoard;

		setHeight(height);
		setWidth(width);

		createBackground(true);
	}

	private void createBackground(boolean basic) {
		String image = "player_hud";
		if (!basic) {
			image = "player_hud_attack";
		}
		bgRegion = resources.gameBoardAtlas.findRegion(image);
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);

		if (!basic) {
			Image ship = new Image(new TextureRegionDrawable(resources.gameBoardAtlas.findRegion("ship")));
			ship.setScaling(Scaling.fillX);
			ship.setWidth(getWidth() * 0.1f);
			ship.setX(getWidth() * 0.5f - ship.getWidth() * 0.5f);
			ship.setY(getHeight() * 0.5f - ship.getHeight() * 0.5f);
			addActor(ship);
		}
	}

	private Color getColor(String handle) {
		if (handle.equals(GameLoop.USER.handle)) {
			return Constants.Colors.USER_SHIP_FILL;
		} else if (!handle.equals(Constants.OWNER_NO_ONE) && !handle.equals(GameLoop.USER.handle)) {
			return Constants.Colors.ENEMY_SHIP_FILL;
		}

		return Constants.Colors.NEUTRAL;
	}

	public void createAttackLabels(List<Move> moves) {
		clear();
		createBackground(true);

		if (moves.isEmpty()) {
			return;
		}

		clear();
		createBackground(false);

		int baseAttack = 0;
		int baseDefense = 0;
		int attackMultiplier = 0;
		int defenseMultiplier = 0;
		String currentOwner = "";
		String previousOwner = "";
		String moveOwner = "";

		Planet planet = gameBoard.getPlanet(moves.get(0).to);

		int userMoveCount = 0;
		int enemyMoveCount = 0;
		int userAttackBonus = 0;
		int enemyAttackBonus = 0;
		boolean didAirAttackOccur = false;
		for (Move move : moves) {
			if (!move.battleStats.diedInAirAttack) {
				baseAttack += move.shipsToMove;
				baseDefense = move.battleStats.previousShipsOnPlanet;
				currentOwner = gameBoard.getPlanet(move.to).owner;
				previousOwner = move.battleStats.previousPlanetOwner;
				attackMultiplier = (int) (move.battleStats.attackMultiplier * 100.0f);
				defenseMultiplier = (int) (move.battleStats.defenceMultiplier * 100.0f);
				moveOwner = move.handle;
			} else {
				didAirAttackOccur = true;
			}
			if (move.handle.equals(GameLoop.USER.handle)) {
				userMoveCount += move.battleStats.startFleet;
				userAttackBonus = (int) (move.battleStats.attackMultiplier * 100.0f);
			} else {
				enemyMoveCount += move.battleStats.startFleet;
				enemyAttackBonus = (int) (move.battleStats.attackMultiplier * 100.0f);
			}
		}
		if (previousOwner.equals("")) {
			previousOwner = Constants.OWNER_NO_ONE;
		}

		String attackText = "";
		Color color = Color.WHITE;
		Color attackColor = Color.WHITE;
		Color defenseColor = Color.WHITE;
		if (previousOwner.equals(currentOwner) && moveOwner.equals(currentOwner)) {
			attackText = "Transferred";
			attackColor = getColor(currentOwner);
			defenseColor = getColor(currentOwner);
		} else if (previousOwner.equals(currentOwner) && !moveOwner.equals(currentOwner)) {
			attackText = "Attack\nDefended";
			color = planet.getColor(currentOwner, false);
			attackColor = getColor(moveOwner);
			defenseColor = getColor(currentOwner);
		} else if (!previousOwner.equals(currentOwner) && !moveOwner.equals(GameLoop.USER.handle)
				&& previousOwner.equals(GameLoop.USER.handle)) {
			attackText = "Planet\nLost";
			color = planet.getColor(currentOwner, false);
			attackColor = getColor(moveOwner);
			defenseColor = getColor(previousOwner);
		} else if (!previousOwner.equals(currentOwner) && !moveOwner.equals(GameLoop.USER.handle)
				&& !previousOwner.equals(GameLoop.USER.handle)) {
			attackText = "Planet\nCaptured";
			color = planet.getColor(currentOwner, false);
			attackColor = getColor(moveOwner);
			defenseColor = getColor(previousOwner);
		} else if (!previousOwner.equals(currentOwner) && moveOwner.equals(GameLoop.USER.handle)) {
			attackText = "Planet\nCaptured";
			color = planet.getColor(moveOwner, false);
			attackColor = getColor(moveOwner);
			defenseColor = getColor(previousOwner);
		}

		float[] yPos;
		if (didAirAttackOccur) {
			yPos = new float[] { getHeight() * 0.59f, getHeight() * 0.25f, getHeight() * 0.00f };
		} else {
			yPos = new float[] { getHeight() * 0.52f, getHeight() * 0.12f };
		}

		int index = 0;
		if (didAirAttackOccur) {
			{
				ShaderLabel label = new ShaderLabel(resources.fontShader, "Battle:", resources.skin,
						Constants.UI.SMALL_FONT);
				label.setX(getWidth() * 0.01f);
				label.setY(yPos[index]);
				label.setWidth(getWidth() * 0.25f);
				label.setAlignment(Align.left, Align.left);
				addActor(label);
			}

			{
				ShaderLabel label = new ShaderLabel(resources.fontShader, "" + userMoveCount, resources.skin,
						Constants.UI.SMALL_FONT);
				label.setColor(Constants.Colors.USER_SHIP_FILL);
				label.setX(getWidth() * 0.25f);
				label.setY(yPos[index] + getHeight() * 0.08f);
				label.setWidth(getWidth() * 0.05f);
				label.setAlignment(Align.right, Align.right);
				addActor(label);
			}

			if (userAttackBonus > 0) {
				ShaderLabel label = new ShaderLabel(resources.fontShader, "+" + userAttackBonus + "%", resources.skin,
						Constants.UI.SMALL_FONT);
				label.setColor(Constants.Colors.USER_SHIP_FILL);
				label.setX(getWidth() * 0.25f);
				label.setY(yPos[index] + getHeight() * 0.08f);
				label.setWidth(getWidth() * 0.19f);
				label.setAlignment(Align.right, Align.right);
				addActor(label);
			}

			{
				ShaderLabel label = new ShaderLabel(resources.fontShader, "" + enemyMoveCount, resources.skin,
						Constants.UI.SMALL_FONT);
				label.setColor(Constants.Colors.ENEMY_SHIP_FILL);
				label.setX(getWidth() * 0.25f);
				label.setY(yPos[index] - getHeight() * 0.13f);
				label.setWidth(getWidth() * 0.05f);
				label.setAlignment(Align.right, Align.right);
				addActor(label);
			}

			if (enemyAttackBonus > 0) {
				ShaderLabel label = new ShaderLabel(resources.fontShader, "+" + enemyAttackBonus + "%", resources.skin,
						Constants.UI.SMALL_FONT);
				label.setColor(Constants.Colors.ENEMY_SHIP_FILL);
				label.setX(getWidth() * 0.25f);
				label.setY(yPos[index] - getHeight() * 0.13f);
				label.setWidth(getWidth() * 0.19f);
				label.setAlignment(Align.right, Align.right);
				addActor(label);
			}

			index++;
		}

		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "Attack:", resources.skin,
					Constants.UI.SMALL_FONT);
			label.setX(getWidth() * 0.01f);
			label.setY(yPos[index]);
			label.setWidth(getWidth() * 0.25f);
			label.setAlignment(Align.left, Align.left);
			addActor(label);
		}

		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "" + baseAttack, resources.skin,
					Constants.UI.SMALL_FONT);
			label.setX(getWidth() * 0.25f);
			label.setY(yPos[index]);
			label.setWidth(getWidth() * 0.05f);
			label.setAlignment(Align.right, Align.right);
			label.setColor(attackColor);
			addActor(label);
		}
		if (attackMultiplier > 0) {
			ShaderLabel label = new ShaderLabel(resources.fontShader, "+" + attackMultiplier + "%", resources.skin,
					Constants.UI.SMALL_FONT);
			label.setX(getWidth() * 0.25f);
			label.setY(yPos[index]);
			label.setWidth(getWidth() * 0.19f);
			label.setAlignment(Align.right, Align.right);
			label.setColor(attackColor);
			addActor(label);
		}
		index++;

		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "Defense:", resources.skin,
					Constants.UI.SMALL_FONT);
			label.setX(getWidth() * 0.01f);
			label.setY(yPos[index]);
			label.setWidth(getWidth() * 0.25f);
			label.setAlignment(Align.left, Align.left);
			addActor(label);
		}

		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "" + baseDefense, resources.skin,
					Constants.UI.SMALL_FONT);
			label.setX(getWidth() * 0.25f);
			label.setY(yPos[index]);
			label.setWidth(getWidth() * 0.05f);
			label.setAlignment(Align.right, Align.right);
			label.setColor(defenseColor);
			addActor(label);
		}
		if (defenseMultiplier > 0) {
			ShaderLabel label = new ShaderLabel(resources.fontShader, "+" + defenseMultiplier + "%", resources.skin,
					Constants.UI.SMALL_FONT);
			label.setX(getWidth() * 0.25f);
			label.setY(yPos[index]);
			label.setWidth(getWidth() * 0.19f);
			label.setAlignment(Align.right, Align.right);
			label.setColor(defenseColor);
			addActor(label);
		}

		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, attackText, resources.skin,
					Constants.UI.DEFAULT_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setColor(color);
			label.setX(getWidth() * 0.5f);
			label.setY(getHeight() * 0.42f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.5f);
			label.setAlignment(Align.center, Align.center);

			addActor(label);
		}
	}
}

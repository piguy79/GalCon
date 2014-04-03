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
		String currentOwner = "";
		String previousOwner = "";
		String moveOwner = "";

		Planet planet = gameBoard.getPlanet(moves.get(0).to);

		for (Move move : moves) {
			baseAttack += move.shipsToMove;
			baseDefense = move.battleStats.previousShipsOnPlanet;
			currentOwner = gameBoard.getPlanet(move.to).owner;
			previousOwner = move.battleStats.previousPlanetOwner;
			moveOwner = move.handle;
		}
		if (previousOwner.equals("")) {
			previousOwner = Constants.OWNER_NO_ONE;
		}

		String attackText = "";
		Color color = Color.WHITE;
		if (previousOwner.equals(currentOwner) && moveOwner.equals(currentOwner)) {
			attackText = "Transferred";
		} else if (previousOwner.equals(currentOwner) && !moveOwner.equals(currentOwner)) {
			attackText = "Attack\nDefended";
			color = planet.getColor(currentOwner);
		} else if (!previousOwner.equals(currentOwner) && !moveOwner.equals(GameLoop.USER.handle)
				&& previousOwner.equals(GameLoop.USER.handle)) {
			attackText = "Planet\nLost";
			color = planet.getColor(currentOwner);
		} else if (!previousOwner.equals(currentOwner) && !moveOwner.equals(GameLoop.USER.handle)
				&& !previousOwner.equals(GameLoop.USER.handle)) {
			attackText = "Planet\nCaptured";
			color = planet.getColor(currentOwner);
		} else if (!previousOwner.equals(currentOwner) && moveOwner.equals(GameLoop.USER.handle)) {
			attackText = "Planet\nCaptured";
			color = planet.getColor(moveOwner);
		}

		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "Attack:", resources.skin,
					Constants.UI.DEFAULT_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getWidth() * 0.05f);
			label.setY(getHeight() * 0.64f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.25f);
			label.setAlignment(Align.left, Align.left);
			addActor(label);
		}

		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "" + baseAttack, resources.skin,
					Constants.UI.DEFAULT_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getWidth() * 0.25f);
			label.setY(getHeight() * 0.64f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.15f);
			label.setAlignment(Align.right, Align.right);
			addActor(label);
		}

		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "Defense:", resources.skin,
					Constants.UI.DEFAULT_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getWidth() * 0.05f);
			label.setY(getHeight() * 0.25f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.25f);
			label.setAlignment(Align.left, Align.left);
			addActor(label);
		}

		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "" + baseDefense, resources.skin,
					Constants.UI.DEFAULT_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getWidth() * 0.25f);
			label.setY(getHeight() * 0.25f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.15f);
			label.setAlignment(Align.right, Align.right);
			addActor(label);
		}

		{
			Group group = new Group();
			group.setColor(Color.WHITE);
			group.setX(getWidth() * 0.5f);
			group.setY(0);
			group.setWidth(getWidth() * 0.5f);
			group.setHeight(getHeight());
			addActor(group);
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

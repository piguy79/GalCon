package com.xxx.galcon.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class MoveButton extends Group implements Comparable<MoveButton> {

	private static final Color NEW_MOVE = Color.valueOf("E8920C");
	private AtlasRegion bgTexture;
	private Move move;
	private GameBoard gameBoard;

	private Resources resources;

	public MoveButton(Resources resources, GameBoard gameBoard, Move move, float width, float height) {
		super();
		this.resources = resources;
		this.move = move;
		this.gameBoard = gameBoard;
		setWidth(width);
		setHeight(height);

		createLayout();
	}

	private void createLayout() {
		bgTexture = resources.gameBoardAtlas.findRegion("bottom_bar_ship_button");
		createBackground();

		addLabels();
	}

	public boolean isActive() {
		return move.startingRound == gameBoard.roundInformation.round && !GameLoop.USER.hasMoved(gameBoard);
	}

	private void addLabels() {
		float padX = getWidth() * 0.1f;
		float padY = getHeight() * 0.1f;

		ShaderLabel duration = new ShaderLabel(resources.fontShader, "" + (int) Math.ceil(move.duration),
				resources.skin, Constants.UI.DEFAULT_FONT_BLACK);
		duration.setX(getWidth() - (duration.getTextBounds().width + padX));
		duration.setY(getHeight() - (duration.getTextBounds().height + padY));

		ShaderLabel fleet = new ShaderLabel(resources.fontShader, "" + move.shipsToMove, resources.skin,
				Constants.UI.DEFAULT_FONT_BLACK);
		fleet.setX(padX);
		fleet.setY(padY + fleet.getStyle().font.getDescent());

		addActor(duration);
		addActor(fleet);
	}

	private void createBackground() {
		bgTexture = resources.gameBoardAtlas.findRegion("bottom_bar_ship_button");
		Image backGround = new Image(new TextureRegionDrawable(bgTexture));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());

		if (isActive()) {
			backGround.addAction(Actions.color(NEW_MOVE, 0.4f));
		}

		addActor(backGround);
	}

	public Move getMove() {
		return move;
	}

	@Override
	public int compareTo(MoveButton otherMove) {
		if (this.move.startingRound < otherMove.getMove().startingRound) {
			return -1;
		} else if (this.move.startingRound > otherMove.getMove().startingRound) {
			return 1;
		}
		return 0;
	}

}

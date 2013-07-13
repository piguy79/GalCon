package com.xxx.galcon.screen.hud;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.InGameInputProcessor.TouchPoint;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.screen.BoardScreen;

public class BoardScreenHud extends Hud {
	public static int MAX_BAR_WIDTH_FOR_MOVES = 0;
	public static final float BOTTOM_HEIGHT_RATIO = 0.13f;
	private GameBoard gameBoard;

	private HudButton endTurnButton;
	private Texture bottomBar;
	private Texture bottomBarExpandButton;
	private BoardScreen boardScreen;
	private AssetManager assetManager;
	private Map<Move, ShipMoveHudButton> moveToButtonMap = new HashMap<Move, ShipMoveHudButton>();

	public BoardScreenHud(BoardScreen boardScreen, AssetManager assetManager) {
		super();
		this.assetManager = assetManager;
		this.boardScreen = boardScreen;

		endTurnButton = new EndTurnHudButton(assetManager);

		bottomBar = assetManager.get("data/images/bottom_bar.png", Texture.class);
		bottomBarExpandButton = assetManager.get("data/images/bottom_bar_expand_button.png", Texture.class);

		addHudButton(endTurnButton);

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	private boolean redrawMoves = false;

	public void associateCurrentRoundInformation(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
		this.moveToButtonMap.clear();

		for (ListIterator<HudButton> iter = getHudButtons().listIterator(); iter.hasNext();) {
			HudButton hudButton = iter.next();
			if (hudButton instanceof ShipMoveHudButton) {
				iter.remove();
			}
		}

		redrawMoves = true;
	}

	@Override
	public void resize(int width, int height) {
		MAX_BAR_WIDTH_FOR_MOVES = (int) (width * 0.75f);

		super.resize(width, height);
	}

	@Override
	public void render(float delta) {
		getSpriteBatch().begin();

		getSpriteBatch().draw(bottomBar, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * BOTTOM_HEIGHT_RATIO);

		if (gameBoard.wasADraw() || gameBoard.hasWinner()) {
			endTurnButton.setEnabled(false);
		} else if (GameLoop.USER.hasMoved(gameBoard)) {
			endTurnButton.setEnabled(false);
		} else {
			endTurnButton.setEnabled(true);
		}

		getSpriteBatch().end();

		InGameInputProcessor ip = (InGameInputProcessor) Gdx.input.getInputProcessor();

		if (ip.isDragging()) {
			List<TouchPoint> dragPoints = ip.getDragTouchPoints();

			TouchPoint dragBegin = dragPoints.get(0);
			if (dragBegin.x > 0 && dragBegin.x < MAX_BAR_WIDTH_FOR_MOVES) {
				if (dragBegin.y > 0 && dragBegin.y < Gdx.graphics.getHeight() * BOTTOM_HEIGHT_RATIO) {
					int size = dragPoints.size();
					TouchPoint current = dragPoints.get(size - 1);
					TouchPoint previous = dragPoints.get(size - 2);

					for (HudButton button : getHudButtons()) {
						if (!(button instanceof ShipMoveHudButton)) {
							continue;
						}

						ShipMoveHudButton moveButton = (ShipMoveHudButton) button;
						moveButton.offSet(current.x - previous.x);
					}
				}
			}
		}

		if (redrawMoves) {
			getSpriteBatch().begin();
			addMoveButtons(boardScreen.getPendingMoves(), true);
			addMoveButtons(gameBoard.movesInProgress, false);
			getSpriteBatch().end();
			redrawMoves = false;
		}

		super.render(delta);
	}

	private void addMoveButtons(List<Move> moves, boolean isPending) {
		int size = moves.size();

		Collections.sort(moves, moveComparator);

		for (int i = 0; i < size; ++i) {
			Move move = moves.get(i);
			if (!move.playerHandle.equals(GameLoop.USER.handle)) {
				continue;
			}
			if (!moveToButtonMap.containsKey(move)) {
				ShipMoveHudButton button = new ShipMoveHudButton(move, isPending, moveToButtonMap.size(), assetManager);
				addHudButton(button);
				moveToButtonMap.put(move, button);

				resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			}
		}
	}

	private Comparator<Move> moveComparator = new Comparator<Move>() {
		@Override
		public int compare(Move o1, Move o2) {
			if (o1.duration < o2.duration) {
				return -1;
			}

			if (o1.duration > o2.duration) {
				return 1;
			}

			if (o1.shipsToMove > o2.shipsToMove) {
				return -1;
			}

			return 1;
		}
	};

}

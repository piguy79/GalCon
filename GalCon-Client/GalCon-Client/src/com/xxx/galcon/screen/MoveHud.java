package com.xxx.galcon.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.event.MoveEvent;
import com.xxx.galcon.screen.event.SendMoveEvent;
import com.xxx.galcon.screen.widget.ActionButton;

public class MoveHud extends Table {

	private AtlasRegion bgTexture;
	private Map<Move, MoveButton> moves;
	private Table moveButtonHolder;
	private ScrollPane scrollPane;
	private GameBoard gameBoard;

	private Resources resources;

	public MoveHud(Resources resources, GameBoard gameBoard, float width, float height) {
		super();
		this.moves = new HashMap<Move, MoveButton>();
		this.resources = resources;
		this.gameBoard = gameBoard;
		setWidth(width);
		setHeight(height);
		setX(0);
		setY(0);
		createTable();
		addMoveButtons();
		addPerformMoveButton();
	}

	private void createTable() {
		bgTexture = resources.gameBoardAtlas.findRegion("bottom_bar");
		setBackground(new TextureRegionDrawable(bgTexture));
	}

	private void addPerformMoveButton() {
		if (!GameLoop.USER.hasMoved(gameBoard)) {
			final ActionButton performMove = new ActionButton(resources.skin, "performMoveButton", new Point(getX()
					+ (getWidth() * 0.83f), getY() + (getHeight() * 0.05f)));
			performMove.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
						List<Move> newMoves = new ArrayList<Move>();
						for (Move move : gameBoard.movesInProgress) {
							if (move.belongsToPlayer(GameLoop.USER)
									&& move.startingRound == gameBoard.roundInformation.round) {
								newMoves.add(move);
							}
						}
						fire(new SendMoveEvent(newMoves));
				}
			});
			addActor(performMove);
		}
	}

	private void addMoveButtons() {
		moveButtonHolder = new Table();
		moveButtonHolder.setWidth(getWidth() * 0.7f);
		moveButtonHolder.setHeight(getHeight() * 0.95f);

		moveButtonHolder.pad(getWidth() * 0.05f);

		moveButtonHolder.left().bottom().padLeft(5).padRight(getWidth() * 0.4f).padBottom(getHeight() * 0.12f)
				.defaults().padRight(getWidth() * 0.01f).width(getWidth() * 0.1f).height(getHeight() * 0.85f);

		scrollPane = new ScrollPane(moveButtonHolder);
		scrollPane.setScrollingDisabled(false, true);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setWidth(moveButtonHolder.getWidth());

		addActor(scrollPane);
	}

	private void addMoveToMap(final Move move) {
		if (moves.get(move) == null) {
			float buttonWidth = Gdx.graphics.getWidth() * 0.09f;
			MoveButton button = new MoveButton(resources, gameBoard, move, buttonWidth, getHeight() * 0.85f);

			button.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					fire(new MoveEvent(0, move));
				}
			});

			moves.put(move, button);
		} else {
			moves.remove(move);
			addMoveToMap(move);
		}
	}

	public void addMove(Move move) {
		if (move.handle.equals(GameLoop.USER.handle) && move.duration > 0) {
			addMoveToMap(move);
			renderMoves();
		}
	}

	public void renderMoves() {
		moveButtonHolder.clearChildren();
		List<MoveButton> movesToDisplay = new ArrayList<MoveButton>();
		movesToDisplay.addAll(moves.values());
		Collections.sort(movesToDisplay);
		Collections.sort(movesToDisplay, new Comparator<MoveButton>() {
			@Override
			public int compare(MoveButton move1, MoveButton move2) {
				if (move1.getMove().duration < move2.getMove().duration) {
					return -1;
				} else if (move1.getMove().duration > move2.getMove().duration) {
					return 1;
				}
				return 0;
			}
		});
		for (MoveButton button : movesToDisplay) {
			moveButtonHolder.add(button);
		}
	}

	public void removeMove(final Move move) {
		if (!moves.containsKey(move)) {
			return;
		}

		moves.get(move).addAction(sequence(fadeOut(0.4f), new RunnableAction() {
			@Override
			public void run() {
				moves.remove(move);
				renderMoves();
			}
		}));

	}

	public void removeMoves() {
		moves.clear();
		renderMoves();
	}

	private Set<Move> savedMoves = new HashSet<Move>();

	public void saveMoves() {
		savedMoves.clear();
		savedMoves.addAll(moves.keySet());
	}

	public void restoreMoves() {
		moves.clear();
		for (Move move : savedMoves) {
			addMove(move);
		}
	}
}

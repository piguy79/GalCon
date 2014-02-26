package com.xxx.galcon.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static java.lang.Math.floor;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.math.GalConMath;
import com.xxx.galcon.model.Bounds;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.model.Size;
import com.xxx.galcon.model.factory.MoveFactory;
import com.xxx.galcon.model.factory.PlanetButtonFactory;
import com.xxx.galcon.screen.event.DialogEventListener;
import com.xxx.galcon.screen.event.MoveListener;
import com.xxx.galcon.screen.event.RefreshEvent;
import com.xxx.galcon.screen.event.ResignEvent;
import com.xxx.galcon.screen.event.TransitionEventListener;
import com.xxx.galcon.screen.hud.PlanetInfoHud;
import com.xxx.galcon.screen.hud.ShipSelectionHud;
import com.xxx.galcon.screen.hud.SingleMoveInfoHud;
import com.xxx.galcon.screen.overlay.DismissableOverlay;
import com.xxx.galcon.screen.overlay.Overlay;
import com.xxx.galcon.screen.overlay.TextOverlay;
import com.xxx.galcon.screen.ship.selection.BoardScreenOptionsDialog;
import com.xxx.galcon.screen.ship.selection.PlanetInformationDialog;
import com.xxx.galcon.screen.widget.Line;
import com.xxx.galcon.screen.widget.Moon;
import com.xxx.galcon.screen.widget.PlanetButton;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class BoardScreen implements ScreenFeedback {

	private int roundAnimated = -2;
	private Camera camera;
	private GameBoard gameBoard;
	private List<Move> allMoves;

	public List<Planet> touchedPlanets = new ArrayList<Planet>(2);
	List<HarvestMove> inProgressHarvest = new ArrayList<HarvestMove>();

	boolean intro = true;
	float introTimeBegin = 0.0f;
	float introElapsedTime = 2.8f;

	private String returnCode = null;

	private Stage stage;

	private Image boardTable;
	private InputProcessor oldInputProcessor;
	private ShipSelectionHud shipSelectionHud;
	private SingleMoveInfoHud moveInfoHud;
	private PlanetInfoHud planetInfoHud;
	private ShaderLabel moveShipCount;
	private MoveHud moveHud;
	private BoardScreenPlayerHud playerHud;

	private List<Moon> moons = new ArrayList<Moon>();

	private Map<String, PlanetButton> planetButtons = new HashMap<String, PlanetButton>();
	private Map<String, Integer> planetToMoveCount = new HashMap<String, Integer>();

	private boolean planetMoveChange = false;

	private MenuScreenContainer previousScreen;

	private Overlay overlay;

	private Resources resources = null;

	/**
	 * Stores and pre-calculates common sizes and ratios of elements on the
	 * baord screen.
	 */
	public static class ScreenCalculations {
		private static float HUD_BOTTOM_HEIGHT_RATIO = 0.1f;
		private static float HUD_TOP_HEIGHT_RATIO = 0.1f;
		private static float BOARD_SCREEN_RATIO = 1.0f - HUD_BOTTOM_HEIGHT_RATIO - HUD_TOP_HEIGHT_RATIO;

		private Bounds worldBounds = null;
		private Bounds boardBounds = null;
		private Bounds bottomHudBounds = null;
		private Bounds topHudBounds = null;

		public ScreenCalculations(int worldWidth, int worldHeight) {
			this.worldBounds = new Bounds(new Point(0, 0), new Size(worldWidth, worldHeight));

			updateBoardCalcs();
		}

		private void updateBoardCalcs() {
			Size boardSizeInPixels = new Size(worldBounds.size.width,
					(int) (worldBounds.size.height * BOARD_SCREEN_RATIO));
			Size bottomHudInPixels = new Size(worldBounds.size.width,
					(int) (worldBounds.size.height * HUD_BOTTOM_HEIGHT_RATIO));
			Size topHudInPixels = new Size(worldBounds.size.width,
					(int) (worldBounds.size.height * HUD_TOP_HEIGHT_RATIO));

			bottomHudBounds = new Bounds(new Point(0, 0), bottomHudInPixels);
			boardBounds = new Bounds(new Point(0, bottomHudBounds.getTopY() + 1), boardSizeInPixels);
			topHudBounds = new Bounds(new Point(0, boardBounds.getTopY() + 1), topHudInPixels);
		}

		public Bounds getBoardBounds() {
			return boardBounds;
		}

		public Bounds getBottomHudBounds() {
			return bottomHudBounds;
		}

		public Bounds getTopHudBounds() {
			return topHudBounds;
		}

		public Bounds getWorldBounds() {
			return worldBounds;
		}
	}

	public static class BoardCalculations {

		private int maxPlanetRadius;
		private int minPlanetRadius;

		private Size boardTiles = null;
		private Size tileSize = null;

		private ScreenCalculations screenCalcs;

		public BoardCalculations(ScreenCalculations screenCalcs, int tilesWide, int tilesHigh) {
			this.screenCalcs = screenCalcs;
			this.boardTiles = new Size(tilesWide, tilesHigh);

			updateTileCalcs();
			updatePlanetCalcs();
		}

		private void updateTileCalcs() {
			tileSize = new Size((int) (screenCalcs.getBoardBounds().size.width / boardTiles.width),
					(int) (screenCalcs.getBoardBounds().size.height / boardTiles.height));
		}

		private void updatePlanetCalcs() {
			int largest = Math.max(tileSize.width, tileSize.height);
			maxPlanetRadius = largest;
			minPlanetRadius = (int) (largest * 0.5f);
		}

		/**
		 * Produce a point that is centered in the middle of the given tile.
		 */
		public Point tileCoordsToPixels(Point point) {
			float yOffset = screenCalcs.getBoardBounds().origin.y;

			return new Point(tileSize.width * point.x + tileSize.width * 0.5f, tileSize.height * point.y + yOffset
					+ tileSize.height * 0.5f);
		}

		public int getMaxPlanetRadius() {
			return maxPlanetRadius;
		}

		public int getMinPlanetRadius() {
			return minPlanetRadius;
		}

		public Size getTileSize() {
			return tileSize;
		}
	}

	public ScreenCalculations screenCalcs = null;
	public BoardCalculations boardCalcs = null;

	public BoardScreen(Resources resources, TweenManager tweenManager) {
		this.resources = resources;

		this.allMoves = new ArrayList<Move>();

		stage = new Stage();

		// stage.addListener(new ClickListener() {
		// @Override
		// public void clicked(InputEvent event, float x, float y) {
		// super.clicked(event, x, y);
		// stage.getRoot().fire(event);
		// }
		// });
	}

	public void setGameBoard(GameBoard gameBoard) {
		clearTouchedPlanets();
		planetToMoveCount.clear();
		inProgressHarvest.clear();
		allMoves.clear();

		stage = new Stage();
		stage.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		this.gameBoard = gameBoard;
		for (Move move : gameBoard.movesInProgress) {
			allMoves.add(move);
		}
		planetButtons.clear();
		planetToMoveCount.clear();
		if (moveHud != null) {
			moveHud.removeMoves();
		}
		planetMoveChange = true;

		createLayout();
	}

	private void createLayout() {
		AtlasRegion bg = resources.levelAtlas.findRegion("" + gameBoard.map);

		screenCalcs = new ScreenCalculations(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		boardCalcs = new BoardCalculations(screenCalcs, gameBoard.widthInTiles, gameBoard.heightInTiles);

		boardTable = new Image(new TextureRegionDrawable(bg));
		screenCalcs.getBoardBounds().applyBounds(boardTable);

		boardTable.addListener(clearPlanetListener());

		stage.addActor(boardTable);
		createGrid();
		createMoveHud();
		createPlayerHud();
		createPlanets();
		createMoves();
		createHarvest();

		Gdx.input.setInputProcessor(stage);

		createEndGameOverlay();
	}

	private void createEndGameOverlay() {
		if (gameBoard.hasWinner()) {
			String endGameMessage;
			if (gameBoard.endGameInformation.winnerHandle.equals(GameLoop.USER.handle)) {
				endGameMessage = "Winner Text";
			} else {
				endGameMessage = "Loser Text";
			}
			TextOverlay overlay = new TextOverlay(endGameMessage, resources);
			overlay.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					stage.dispose();
					returnCode = Action.BACK;
				}
			});
			stage.addActor(overlay);
		}
	}

	private void createHarvest() {
		for (Moon moon : moons) {
			if (moon.associatedPlanet.isUnderHarvest() && moon.getActions().size == 0) {
				addActionToMoon(moon);
			}
		}

	}

	private void addActionToMoon(Moon moon) {
		float duration = 1.5f;
		moon.addAction(Actions.forever(Actions.sequence(Actions.color(new Color(0, 0, 0, 1), duration),
				Actions.color(new Color(0.9f, 0, 0, 1), duration))));
	}

	private void createPlayerHud() {
		playerHud = new BoardScreenPlayerHud(resources, screenCalcs.getTopHudBounds(), gameBoard);
		playerHud.addListener(new TransitionEventListener() {
			@Override
			public void transition(String action) {
				if (action.equals(Action.BACK)) {
					stage.dispose();
					returnCode = action;
				} else if (action.equals(Action.OPTIONS)) {
					BoardScreenOptionsDialog dialog = new BoardScreenOptionsDialog(resources,
							Gdx.graphics.getWidth() * 0.8f, Gdx.graphics.getHeight() * 0.3f, stage);
					float dialogY = Gdx.graphics.getHeight() - (dialog.getHeight() + (dialog.getHeight() * 0.5f));
					dialog.setX(-dialog.getWidth());
					dialog.setY(dialogY);
					stage.addActor(dialog);
					dialog.show(new Point(Gdx.graphics.getWidth() * 0.1f, dialogY));

					dialog.addListener(new EventListener() {
						@Override
						public boolean handle(Event event) {
							if (event instanceof ResignEvent) {
								overlay = new TextOverlay("Refreshing", resources);
								stage.addActor(overlay);
								UIConnectionWrapper.resignGame(new UpdateBoardScreenResultHandler("Could not refresh"),
										gameBoard.id, GameLoop.USER.handle);
								return true;
							} else if (event instanceof RefreshEvent) {
								overlay = new TextOverlay("Refreshing", resources);
								stage.addActor(overlay);
								UIConnectionWrapper.findGameById(
										new UpdateBoardScreenResultHandler("Could not resign"), gameBoard.id,
										GameLoop.USER.handle);
								return true;
							}
							return false;
						}
					});
				}
			}
		});
		stage.addActor(playerHud);
	}

	private void createMoves() {
		for (Move move : allMoves) {
			if (move.belongsToPlayer(GameLoop.USER) || move.executed) {
				Image movetoDisplay = MoveFactory.createShipForDisplay(move.angleOfMovement(), move.previousPosition,
						resources, boardCalcs);

				boolean showMove = true;

				Point newShipPosition = MoveFactory.getShipPosition(movetoDisplay, move.currentPosition, boardCalcs);
				if (!roundHasAlreadyBeenAnimated()) {
					movetoDisplay.addAction(moveTo(newShipPosition.x, newShipPosition.y, 1.2f));
				} else {
					movetoDisplay.setPosition(newShipPosition.x, newShipPosition.y);
				}

				Color color = Color.GREEN;
				if (!move.belongsToPlayer(GameLoop.USER)) {
					color = Color.RED;
				}

				movetoDisplay.setColor(color);

				if (move.executed && !roundHasAlreadyBeenAnimated()) {
					movetoDisplay.addAction(scaleTo(0, 0, 0.8f));
					addExplosion(move, 0.8f, color);
				} else if (move.executed && roundHasAlreadyBeenAnimated()) {
					showMove = false;
				}
				movetoDisplay.setTouchable(Touchable.disabled);

				if (showMove) {
					stage.addActor(movetoDisplay);
				}
			}
		}

		roundAnimated = gameBoard.roundInformation.currentRound;
	}

	private void addExplosion(Move move, float delay, Color color) {
		int minNumberOfParticles = 6;
		int maxNumberOfParticles = 15;
		float ratio = ((float) move.shipsToMove) / 30.0f;
		int numberOfParticles = min(maxNumberOfParticles, (int) floor(minNumberOfParticles
				+ (maxNumberOfParticles - minNumberOfParticles) * ratio));

		float tileWidth = boardCalcs.getTileSize().width;

		float circleInRadians = (float) Math.PI * 2.0f;
		float startRadius = tileWidth * 0.05f;
		float particleSize = tileWidth * 0.2f;
		float radiansBetweenParticles = circleInRadians / (float) numberOfParticles;

		Point tileCenter = boardCalcs.tileCoordsToPixels(move.endPosition);

		for (float currentAngle = 0; currentAngle < circleInRadians; currentAngle += radiansBetweenParticles) {
			Image particle = new Image(resources.skin, Constants.UI.EXPLOSION_PARTICLE);

			float yStartOffset = (float) Math.sin(currentAngle) * startRadius;
			float xStartOffset = (float) Math.cos(currentAngle) * startRadius;

			particle.setColor(Color.CLEAR);
			particle.setOrigin(particleSize * 0.5f, particleSize * 0.5f);
			particle.setBounds(tileCenter.x + xStartOffset, tileCenter.y + yStartOffset, particleSize, particleSize);
			particle.addAction(sequence(delay(delay), color(color)));
			particle.addAction(sequence(delay(delay),
					moveBy(xStartOffset * 12, yStartOffset * 12, 1.5f, Interpolation.circleOut)));
			particle.addAction(sequence(delay(delay + 0.1f), rotateBy(1000, 2.0f)));
			particle.addAction(sequence(delay(delay + 1.0f), alpha(0.0f, 1.5f)));

			stage.addActor(particle);
		}
	}

	private void highlightPath(Planet fromPlanet, Planet toPlanet) {
		float tileWidth = boardCalcs.getTileSize().width;
		float tileHeight = boardCalcs.getTileSize().height;

		float tileDistance = GalConMath.distance(fromPlanet.position, toPlanet.position);
		float numParticles = (float) Math.ceil(tileDistance * 2.0f);

		float particleSize = tileWidth * 0.2f;

		Point startPointInWorld = boardCalcs.tileCoordsToPixels(fromPlanet.position);
		Point endPointInWorld = boardCalcs.tileCoordsToPixels(toPlanet.position);

		float distanceInPixels = (float) sqrt(pow(endPointInWorld.x - startPointInWorld.x, 2)
				+ pow(endPointInWorld.y - startPointInWorld.y, 2));
		float distanceBetweenParticlesInPixels = distanceInPixels / numParticles;

		float angle = (float) Math.atan((endPointInWorld.y - startPointInWorld.y)
				/ (endPointInWorld.x - startPointInWorld.x));

		if (endPointInWorld.x < startPointInWorld.x) {
			angle -= Math.PI;
		}

		float startDelay = 0.0f;
		for (float dist = tileHeight * 0.25f; dist < distanceInPixels; dist += distanceBetweenParticlesInPixels) {
			Image particle = new Image(resources.skin, "shipImage");

			float yStartOffset = (float) Math.sin(angle) * dist;
			float xStartOffset = (float) Math.cos(angle) * dist;

			particle.setColor(Color.YELLOW);
			particle.setOrigin(particleSize * 0.5f, particleSize * 0.5f);
			particle.setBounds(startPointInWorld.x + xStartOffset - particleSize * 0.5f, startPointInWorld.y
					+ yStartOffset - particleSize * 0.5f, particleSize, particleSize);
			particle.rotate((float) Math.toDegrees(angle));
			particle.addAction(sequence(delay(startDelay),
					forever(sequence(color(Color.BLACK, 0.5f), color(Color.YELLOW, 0.5f)))));
			startDelay += 0.1f;

			particle.setY(particle.getY() - screenCalcs.getBoardBounds().origin.y);
			overlay.addActor(particle);
		}
	}

	private void createMoveHud() {
		final List<Move> userMoves = new ArrayList<Move>();

		for (Move move : allMoves) {
			if (move.belongsToPlayer(GameLoop.USER)) {
				userMoves.add(move);
			}
		}

		moveHud = new MoveHud(resources, gameBoard, screenCalcs.getBottomHudBounds().size.width,
				screenCalcs.getBottomHudBounds().size.height);
		moveHud.addMoves(userMoves);

		moveHud.addListener(new MoveListener() {

			@Override
			protected void performMove(Move move) {
				if (overlay != null) {
					overlay.remove();
				}

				if (move.startingRound == gameBoard.roundInformation.currentRound && !GameLoop.USER.hasMoved(gameBoard)) {
					renderMoveDialog(move);
				} else {
					highlight(move);
				}
			}

			@Override
			public void sendMove() {
				List<Move> currentRoundMoves = new ArrayList<Move>();
				for (Move move : allMoves) {
					if (move.belongsToPlayer(GameLoop.USER)
							&& move.startingRound == gameBoard.roundInformation.currentRound) {
						currentRoundMoves.add(move);
					}
				}
				overlay = new TextOverlay("Uploading ship movements", resources);
				stage.addActor(overlay);
				UIConnectionWrapper.performMoves(new UpdateBoardScreenResultHandler("Could not send moves"),
						gameBoard.id, currentRoundMoves, inProgressHarvest);
			}
		});

		stage.addActor(moveHud);
	}

	private void highlight(Move move) {
		highlight(move, move.fromPlanet, move.toPlanet);
	}

	private void highlight(Planet fromPlanet, Planet toPlanet) {
		highlight(null, fromPlanet.name, toPlanet.name);
	}

	private void highlight(final Planet planet) {
		planetInfoHud = new PlanetInfoHud(resources, screenCalcs.getTopHudBounds().size.width,
				screenCalcs.getTopHudBounds().size.height);
		showPlanetInfoHud();
		planetInfoHud.updateRegen((int) planet.shipRegenRate);

		final PlanetButton planetButton = PlanetButtonFactory.createPlanetButtonWithExpansion(planet, gameBoard,
				roundHasAlreadyBeenAnimated(), boardCalcs, resources);
		positionPlanet(planetButton);

		float hudHeight = screenCalcs.getBottomHudBounds().size.height;
		overlay = new DismissableOverlay(resources, new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				removePlanetHighlight();
			}
		});

		planetButton.setY(planetButton.getY() - hudHeight);

		overlay.setBounds(0, hudHeight, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - 2 * hudHeight);

		stage.addActor(overlay);
		overlay.addActor(planetButton);

		moveHud.saveMoves();
		moveHud.removeMoves();
		for (Move move : allMoves) {
			if (move.belongsToPlayer(GameLoop.USER) && move.toPlanet.equals(planet.name)) {
				Planet fromPlanet = gameBoard.getPlanet(move.fromPlanet);
				PlanetButton fromPlanetButton = PlanetButtonFactory.createPlanetButtonWithExpansion(fromPlanet,
						gameBoard, roundHasAlreadyBeenAnimated(), boardCalcs, resources);
				positionPlanet(fromPlanetButton);
				fromPlanetButton.setY(fromPlanetButton.getY() - hudHeight);
				overlay.addActor(fromPlanetButton);
				highlightPath(fromPlanet, planet);

				moveHud.addMove(move);
			}
		}
	}

	private void removePlanetHighlight() {
		overlay.remove();
		hidePlanetInfoHud();
		touchedPlanets.clear();
		moveHud.restoreMoves();
	}

	private void highlight(final Move move, String fromPlanetName, String toPlanetName) {

		Planet fromPlanet = gameBoard.getPlanet(fromPlanetName);
		Planet toPlanet = gameBoard.getPlanet(toPlanetName);

		final PlanetButton fromPlanetButton = PlanetButtonFactory.createPlanetButtonWithExpansion(fromPlanet,
				gameBoard, roundHasAlreadyBeenAnimated(), boardCalcs, resources);
		final PlanetButton toPlanetButton = PlanetButtonFactory.createPlanetButtonWithExpansion(toPlanet, gameBoard,
				roundHasAlreadyBeenAnimated(), boardCalcs, resources);
		positionPlanet(fromPlanetButton);
		positionPlanet(toPlanetButton);

		final Image moveToDisplay;
		if (move != null) {
			moveToDisplay = MoveFactory.createShipForDisplay(move.angleOfMovement(), move.currentPosition, resources,
					boardCalcs);
			if (moveInfoHud == null) {
				moveInfoHud = new SingleMoveInfoHud(resources, screenCalcs.getTopHudBounds().size.width,
						screenCalcs.getTopHudBounds().size.height);
				showMoveInfoHud();
			}
			moveInfoHud.updateDuration(move.duration);
			moveInfoHud.updateShips(move.shipsToMove);
		} else {
			moveToDisplay = null;
			List<Planet> planets = new ArrayList<Planet>();
			planets.add(fromPlanet);
			planets.add(toPlanet);
			Move fakeMove = MoveFactory.createMove(planets, 1, 0);
			if (moveInfoHud == null) {
				moveInfoHud = new SingleMoveInfoHud(resources, screenCalcs.getTopHudBounds().size.width,
						screenCalcs.getTopHudBounds().size.height);
				showMoveInfoHud();
			}
			moveInfoHud.updateDuration(fakeMove.duration);
		}

		float hudHeight = screenCalcs.getBottomHudBounds().size.height;
		overlay = new DismissableOverlay(resources, new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				overlay.remove();
				hideMoveInfoHud();
				hideShipSelectionHud();
				clearTouchedPlanets();
				removePlanetHighlight();

				if (moveToDisplay != null) {
					moveToDisplay.remove();
				}
			}
		});

		fromPlanetButton.setY(fromPlanetButton.getY() - hudHeight);
		toPlanetButton.setY(toPlanetButton.getY() - hudHeight);

		overlay.setBounds(0, hudHeight, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - 2 * hudHeight);

		stage.addActor(overlay);
		overlay.addActor(fromPlanetButton);
		overlay.addActor(toPlanetButton);

		if (moveToDisplay != null) {
			moveToDisplay.setY(moveToDisplay.getY() - hudHeight);
			overlay.addActor(moveToDisplay);
		}

		highlightPath(fromPlanet, toPlanet);
	}

	private ClickListener clearPlanetListener() {
		return new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				clearTouchedPlanets();
			}
		};
	}

	private void createGrid() {
		float yOffset = boardTable.getHeight() / gameBoard.heightInTiles;
		float xOffset = boardTable.getWidth() / gameBoard.widthInTiles;
		Color grey = Color.GRAY;
		grey.a = 0.6f;
		TextureRegion lineRegion = resources.gameBoardAtlas.findRegion("line");

		for (int i = 1; i < gameBoard.heightInTiles; i++) {
			Line line = new Line(grey, Gdx.graphics.getWidth(), lineRegion);
			line.setY((yOffset * i) + boardTable.getY());
			line.setHeight(Gdx.graphics.getHeight() * 0.004f);
			stage.addActor(line);
			line.addListener(clearPlanetListener());

		}

		for (int i = 1; i < gameBoard.widthInTiles; i++) {
			Line horizontalLine = new Line(grey, Gdx.graphics.getWidth() * 0.006f, lineRegion);
			horizontalLine.setY(boardTable.getY());
			horizontalLine.setX(xOffset * i);
			horizontalLine.setHeight(boardTable.getHeight());
			horizontalLine.addListener(clearPlanetListener());
			stage.addActor(horizontalLine);
		}
	}

	private void createPlanets() {
		for (final Planet planet : gameBoard.planets) {
			final PlanetButton planetButton = PlanetButtonFactory.createPlanetButtonWithExpansion(planet, gameBoard,
					roundHasAlreadyBeenAnimated(), boardCalcs, resources);
			positionPlanet(planetButton);

			planetButton.addListener(new ClickListener() {

				@Override
				public void clicked(InputEvent event, float x, float y) {
					planetButton.addAction(Actions.forever(Actions.sequence(
							Actions.color(new Color(0, 0, 0, 0.4f), 0.7f), Actions.color(planet.getColor(), 0.5f))));
					if (touchedPlanets.size() < 2) {
						touchedPlanets.add(planet);
						renderDialog();
					} else {
						clearTouchedPlanets();
					}
				}
			});
			stage.addActor(planetButton);
			planetButtons.put(planet.name, planetButton);
			planetToMoveCount.put(planet.name, 0);

			if (planet.hasAbility()) {
				createMoon(planet);
			}
		}
	}

	private void createMoon(Planet planet) {
		final Moon moon = PlanetButtonFactory.createMoon(resources, planet, boardCalcs);

		PlanetButton associatedAbilityPlanet = planetButtons.get(planet.name);
		float relativeX = associatedAbilityPlanet.centerPoint().x - (moon.getWidth() / 2);
		float relativeY = associatedAbilityPlanet.centerPoint().y - (moon.getHeight() / 2);

		moon.setX(relativeX - boardCalcs.getTileSize().width / 2);
		moon.setY(relativeY);

		moons.add(moon);
		stage.addActor(moon);
	}

	private void positionPlanet(final PlanetButton planetButton) {
		float yOffset = boardTable.getY();
		float tileWidthInWorld = boardCalcs.getTileSize().width;
		float tileHeightInWorld = boardCalcs.getTileSize().height;

		planetButton.setX(tileWidthInWorld * planetButton.planet.position.x);
		planetButton.setY((tileHeightInWorld * planetButton.planet.position.y) + yOffset);
		// planetButton.setColor(planetButton.planet.getColor());

		float xAdjust = (tileWidthInWorld / 2) - (planetButton.getWidth() / 2);
		planetButton.setX(planetButton.getX() + xAdjust);

		float yAdjust = (tileHeightInWorld / 2) - (planetButton.getHeight() / 2);
		planetButton.setY(planetButton.getY() + yAdjust);
	}

	private void renderDialog() {
		if (touchedPlanets.size() > 1) {
			if (GameLoop.USER.hasMoved(gameBoard)) {
				if (planetsAreTheSame(touchedPlanets)) {
					highlight(touchedPlanets.get(0));
				} else {
					highlightNewPlanet();
				}
			} else {
				if (planetsAreTheSame(touchedPlanets)) {
					highlight(touchedPlanets.get(0));
				} else if (!userPlanetInSelection()) {
					highlightNewPlanet();
				} else {
					Planet userPlanet = touchedUserPlanet(touchedPlanets);
					Planet otherPlanet = otherPlanet(touchedPlanets, userPlanet);
					renderMoveDialog(userPlanet, otherPlanet);
				}
			}
		}
	}

	private boolean userPlanetInSelection() {
		for (Planet planet : touchedPlanets) {
			if (planet.isOwnedBy(GameLoop.USER)) {
				return true;
			}
		}
		return false;
	}

	private void highlightNewPlanet() {
		clearMoveActions(touchedPlanets.get(0));
		Planet toKeep = touchedPlanets.get(1);
		touchedPlanets.clear();
		touchedPlanets.add(toKeep);

	}

	private boolean planetsAreTheSame(List<Planet> touchedPlanets2) {
		return touchedPlanets2.get(0).name.equals(touchedPlanets2.get(1).name);
	}

	private void renderPlanetInformationDialog(final Planet planet) {
		int offset = planetToMoveCount.get(planet.name) != null ? planetToMoveCount.get(planet.name) : 0;
		;
		float heightMul = 0.3f;
		if (planet.hasAbility()) {
			heightMul += 0.1f;
		}
		if (planet.isUnderHarvest()) {
			heightMul += 0.1f;
		}
		PlanetInformationDialog dialog = new PlanetInformationDialog(resources, Gdx.graphics.getWidth() * 0.8f,
				Gdx.graphics.getHeight() * heightMul, stage, planet, gameBoard, roundHasAlreadyBeenAnimated(), offset);
		float dialogY = Gdx.graphics.getHeight() - (dialog.getHeight() + (dialog.getHeight() * 0.5f));
		dialog.setX(-dialog.getWidth());
		dialog.setY(dialogY);
		stage.addActor(dialog);
		dialog.show(new Point(Gdx.graphics.getWidth() * 0.1f, dialogY));

		dialog.addListener(new DialogEventListener() {
			@Override
			public void cancelDialog() {
				super.cancelDialog();
				clearMoveActions(planet);
				touchedPlanets.clear();
			}
		});

		dialog.addListener(new MoveListener() {
			@Override
			public void handleHarvest(Planet planet) {
				inProgressHarvest.add(MoveFactory.createHarvestMove(planet));
				for (Moon moon : moons) {
					if (moon.associatedPlanet.name.equals(planet.name)) {
						addActionToMoon(moon);
					}
				}
				clearTouchedPlanets();
				clearMoveActions(planet);
			}

		});
	}

	private Planet otherPlanet(List<Planet> planets, Planet userPlanet) {
		for (Planet planet : planets) {
			if (!planet.name.equals(userPlanet.name)) {
				return planet;
			}
		}

		return null;
	}

	private Planet touchedUserPlanet(List<Planet> planets) {
		for (Planet planet : planets) {

			if (planet.owner != null && planet.owner.equals(GameLoop.USER.handle)) {
				return planet;
			}
		}
		return null;
	}

	private void renderMoveDialog(final Planet one, final Planet two) {
		Integer offSetCount = planetToMoveCount.get(one.name) == null ? 0 : planetToMoveCount.get(one.name);
		shipSelectionHud = new ShipSelectionHud(one, two, offSetCount, 0, gameBoard.roundInformation.currentRound,
				resources);
		showShipSelectionHud();

		highlight(one, two);

		shipSelectionHud.addListener(new MoveListener() {

			@Override
			protected void performMove(Move move) {
				createNewMove(move);
				hideShipSelectionHud();
				hideMoveInfoHud();
				overlay.remove();
			}

			@Override
			public void cancelDialog() {
				clearTouchedPlanets();
				clearMoveActions(one);
				clearMoveActions(two);

				hideMoveInfoHud();
				hideShipSelectionHud();
				overlay.remove();
			}

			@Override
			public void sliderUpdate(int value) {
				updateShipCount(value);
			}
		});
	}

	private void updateShipCount(int value) {
		moveInfoHud.updateShips(value);
		if (moveShipCount == null) {
			moveShipCount = new ShaderLabel(resources.fontShader, "0", resources.skin, Constants.UI.X_LARGE_FONT);
			moveShipCount.setWidth(Gdx.graphics.getWidth());
			moveShipCount.setX(0);
			moveShipCount.setY(Gdx.graphics.getHeight() * 0.5f - moveShipCount.getHeight() * 0.5f);
			moveShipCount.setAlignment(Align.center, Align.center);
			moveShipCount.setTouchable(Touchable.disabled);
			stage.addActor(moveShipCount);
		}
		moveShipCount.clearActions();
		moveShipCount.setText("" + value);
		moveShipCount.setColor(Color.WHITE);
		moveShipCount.addAction(alpha(0.0f, 0.8f));
	}

	private void clearMoveActions(Planet planet) {
		planetButtons.get(planet.name).clearActions();
		planetButtons.get(planet.name).addAction(Actions.color(planet.getColor(), 0.4f));
	}

	private void showShipSelectionHud() {
		shipSelectionHud.setPosition(0, -screenCalcs.getBottomHudBounds().size.height);
		stage.addActor(shipSelectionHud);

		shipSelectionHud.addAction(moveTo(screenCalcs.getBottomHudBounds().origin.x,
				screenCalcs.getBottomHudBounds().origin.y, 0.5f, Interpolation.circleOut));
	}

	private void showMoveInfoHud() {
		moveInfoHud.setPosition(0, Gdx.graphics.getHeight());
		stage.addActor(moveInfoHud);

		moveInfoHud.addAction(moveTo(screenCalcs.getTopHudBounds().origin.x, screenCalcs.getTopHudBounds().origin.y,
				0.5f, Interpolation.circleOut));
	}

	private void showPlanetInfoHud() {
		planetInfoHud.setPosition(0, Gdx.graphics.getHeight());
		stage.addActor(planetInfoHud);

		planetInfoHud.addAction(moveTo(screenCalcs.getTopHudBounds().origin.x, screenCalcs.getTopHudBounds().origin.y,
				0.5f, Interpolation.circleOut));
	}

	private void renderMoveDialog(final Move move) {
		int offset = planetToMoveCount.get(move.fromPlanet(gameBoard.planets).name);
		shipSelectionHud = new ShipSelectionHud(move.fromPlanet(gameBoard.planets), move.toPlanet(gameBoard.planets),
				offset, move.shipsToMove, gameBoard.roundInformation.currentRound, resources);
		showShipSelectionHud();

		highlight(move);

		shipSelectionHud.addListener(new MoveListener() {
			@Override
			public void cancelDialog() {
				deleteMove(move);
				hideShipSelectionHud();
				hideMoveInfoHud();
				overlay.remove();
				overlay = null;
			}

			@Override
			protected void performMove(Move newMove) {
				deleteMove(move);
				createNewMove(newMove);
				hideShipSelectionHud();
				hideMoveInfoHud();
				overlay.remove();
				overlay = null;
			}

			@Override
			public void sliderUpdate(int value) {
				updateShipCount(value);
			}
		});
	}

	private void hideShipSelectionHud() {
		if (shipSelectionHud != null) {
			shipSelectionHud.addAction(sequence(
					moveTo(0, 0 - Gdx.graphics.getHeight() * 0.1f, 0.5f, Interpolation.circleOut), run(new Runnable() {
						@Override
						public void run() {
							shipSelectionHud.remove();
						}
					})));
		}
	}

	private void hideMoveInfoHud() {
		if (moveShipCount != null) {
			moveShipCount.remove();
			moveShipCount = null;
		}

		if (moveInfoHud != null) {
			moveInfoHud.addAction(sequence(moveTo(0, Gdx.graphics.getHeight(), 0.5f, Interpolation.circleOut),
					run(new Runnable() {
						@Override
						public void run() {
							moveInfoHud.remove();
							moveInfoHud = null;
						}
					})));
		}
	}

	private void hidePlanetInfoHud() {
		if (planetInfoHud != null) {
			planetInfoHud.addAction(sequence(moveTo(0, Gdx.graphics.getHeight(), 0.5f, Interpolation.circleOut),
					run(new Runnable() {
						@Override
						public void run() {
							planetInfoHud.remove();
							planetInfoHud = null;
						}
					})));
		}
	}

	private void createNewMove(Move newMove) {
		clearTouchedPlanets();
		if (newMove.startingRound == gameBoard.roundInformation.currentRound) {
			Integer count = planetToMoveCount.get(newMove.fromPlanet);
			planetToMoveCount.put(newMove.fromPlanet, count + newMove.shipsToMove);
		}
		allMoves.add(newMove);
		moveHud.addMove(newMove);

		clearMoveActions(newMove.fromPlanet(gameBoard.planets));
		clearMoveActions(newMove.toPlanet(gameBoard.planets));
		planetMoveChange = true;
	}

	private void deleteMove(Move move) {
		allMoves.remove(move);
		int offset = planetToMoveCount.get(move.fromPlanet);
		offset -= move.shipsToMove;
		planetToMoveCount.put(move.fromPlanet, offset);
		planetMoveChange = true;
		moveHud.removeMove(move);
	}

	private boolean roundHasAlreadyBeenAnimated() {
		return roundAnimated == gameBoard.roundInformation.currentRound;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		if (planetMoveChange) {
			applyMovesToPlanetButtons();
			planetMoveChange = false;
			// renderHarvest();
		}
		if (gameBoard != null) {
			renderMoons();
		}

		stage.act(delta);
		stage.draw();
	}

	private void renderMoons() {
		for (Moon moon : moons) {
			Point newPosition = findMoonPosition(moon);
			if (newPosition != null) {
				moon.setX(newPosition.x - (moon.getWidth() / 2));
				moon.setY(newPosition.y - (moon.getHeight() / 2));
			}
		}
	}

	private Point findMoonPosition(Moon moon) {
		PlanetButton associatedPlanet = planetButtons.get(moon.associatedPlanet.name);
		Point movePoint = null;
		if (associatedPlanet != null && gameBoard != null) {
			if (moon.angle == 360) {
				moon.angle = 0;
			}

			movePoint = GalConMath.nextPointInEllipse(associatedPlanet.centerPoint(),
					boardCalcs.getTileSize().width * 0.6f, boardCalcs.getTileSize().height / 2, moon.angle);
			moon.angle = (float) (moon.angle + moon.rateOfOrbit);
		}

		return movePoint;
	}

	private void applyMovesToPlanetButtons() {
		for (Planet planet : gameBoard.planets) {
			int offSet = planetToMoveCount.get(planet.name);
			planetButtons.get(planet.name)
					.setText(new StringBuilder().append(planet.numberOfShips - offSet).toString());
		}

	}

	private void clearTouchedPlanets() {
		for (Planet planet : touchedPlanets) {
			PlanetButton button = planetButtons.get(planet.name);
			button.setColor(planet.getColor());
			button.clearActions();
		}

		touchedPlanets.clear();

		if (gameBoard == null || gameBoard.planets == null) {
			return;
		}
		for (Planet planet : gameBoard.planets) {
			planet.touched = false;
		}
	}

	@Override
	public void resize(int width, int height) {
		camera = new PerspectiveCamera(67f, width, height);
		camera.near = 1.0f;
		camera.far = 5000f;

		camera.position.set(new Vector3(0, 0, 0));
		camera.translate(0.0f, 0.0f, 10.0f);
		camera.lookAt(0.0f, 0.0f, 0.0f);
		camera.update();

		Gdx.gl.glViewport(0, 0, width, height);

	}

	@Override
	public void show() {
		oldInputProcessor = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(stage);

	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(oldInputProcessor);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {

	}

	@Override
	public Object getRenderResult() {
		return returnCode;
	}

	public void setConnectionError(String msg) {

	}

	public class UpdateBoardScreenResultHandler implements UIConnectionResultCallback<GameBoard> {
		private String errorMessage;

		public UpdateBoardScreenResultHandler(String errorMessage) {
			this.errorMessage = errorMessage + "\n\nPlease try again";
		}

		@Override
		public void onConnectionResult(GameBoard result) {
			setGameBoard(result);
		}

		@Override
		public void onConnectionError(String msg) {
			final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(errorMessage, resources), null);
			stage.addActor(ovrlay);
		}
	}

	@Override
	public void resetState() {
		returnCode = null;
		roundAnimated = -2;
		clearTouchedPlanets();

		gameBoard = null;
		moveShipCount = null;
		moveInfoHud = null;
		moveHud = null;
	}

	public MenuScreenContainer getPreviousScreen() {
		return previousScreen;
	}

	public void setPreviousScreen(MenuScreenContainer previousScreen) {
		this.previousScreen = previousScreen;
	}
	
	public static class Labels {
		public static String waitingLabel(String social){
			if(social != null && !social.isEmpty()){
				return "[waiting for " + social + "]";
			}
			return "[waiting for an opponent]";
		}
	}
}

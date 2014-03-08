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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
import com.xxx.galcon.screen.event.MoveListener;
import com.xxx.galcon.screen.event.RefreshEvent;
import com.xxx.galcon.screen.event.ResignEvent;
import com.xxx.galcon.screen.event.TransitionEventListener;
import com.xxx.galcon.screen.overlay.DismissableOverlay;
import com.xxx.galcon.screen.overlay.HighlightOverlay;
import com.xxx.galcon.screen.overlay.Overlay;
import com.xxx.galcon.screen.overlay.TextOverlay;
import com.xxx.galcon.screen.ship.selection.BoardScreenOptionsDialog;
import com.xxx.galcon.screen.widget.Line;
import com.xxx.galcon.screen.widget.Moon;
import com.xxx.galcon.screen.widget.PlanetButton;

public class BoardScreen implements ScreenFeedback {

	private int roundAnimated = -2;
	private Camera camera;
	private GameBoard gameBoard;

	public List<Planet> touchedPlanets = new ArrayList<Planet>(2);
	List<HarvestMove> inProgressHarvest = new ArrayList<HarvestMove>();

	boolean intro = true;
	float introTimeBegin = 0.0f;
	float introElapsedTime = 2.8f;

	private String returnCode = null;

	private Stage stage;

	private Group boardTable;
	private MoveHud moveHud;
	private BoardScreenPlayerHud playerHud;

	private List<Moon> moons = new ArrayList<Moon>();

	private Map<String, PlanetButton> planetButtons = new HashMap<String, PlanetButton>();

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

		private float maxPlanetRadius;
		private float minPlanetRadius;

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
			tileSize = new Size(screenCalcs.getBoardBounds().size.width / boardTiles.width,
					screenCalcs.getBoardBounds().size.height / boardTiles.height);
		}

		private void updatePlanetCalcs() {
			float largest = Math.max(tileSize.width, tileSize.height);
			maxPlanetRadius = largest;
			minPlanetRadius = (int) (largest * 0.5f);
		}

		/**
		 * Produce a point that is centered in the middle of the given tile.
		 */
		public Point tileCoordsToPixels(Point point) {
			return new Point(tileSize.width * point.x + tileSize.width * 0.5f, tileSize.height * point.y
					+ tileSize.height * 0.5f);
		}

		public void centerPoint(Point point, Actor actor) {
			point.x -= actor.getWidth() * 0.5f;
			point.y -= actor.getHeight() * 0.5f;
		}

		public float getMaxPlanetRadius() {
			return maxPlanetRadius;
		}

		public float getMinPlanetRadius() {
			return minPlanetRadius;
		}

		public Size getTileSize() {
			return tileSize;
		}
	}

	public ScreenCalculations screenCalcs = null;
	public BoardCalculations boardCalcs = null;

	public BoardScreen(Resources resources) {
		this.resources = resources;
		stage = new Stage();
	}

	public void setGameBoard(GameBoard gameBoard) {
		clearTouchedPlanets();
		inProgressHarvest.clear();

		stage = new Stage();
		stage.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		this.gameBoard = gameBoard;

		planetButtons.clear();
		if (moveHud != null) {
			moveHud.removeMoves();
		}

		createLayout();
	}

	private void createLayout() {
		AtlasRegion bg = resources.levelAtlas.findRegion("" + gameBoard.map);

		screenCalcs = new ScreenCalculations(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		boardCalcs = new BoardCalculations(screenCalcs, gameBoard.widthInTiles, gameBoard.heightInTiles);

		boardTable = new Group();
		screenCalcs.getBoardBounds().applyBounds(boardTable);
		Image bgImage = new Image(new TextureRegionDrawable(bg));
		bgImage.setWidth(screenCalcs.getBoardBounds().size.width);
		bgImage.setHeight(screenCalcs.getBoardBounds().size.height);
		boardTable.addActor(bgImage);

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
		Map<String, List<Move>> planetToMoves = new HashMap<String, List<Move>>();
		for (Move move : gameBoard.movesInProgress) {
			if (move.belongsToPlayer(GameLoop.USER) || move.executed) {
				List<Move> planetMoves;
				if (planetToMoves.containsKey(move.toPlanet)) {
					planetMoves = planetToMoves.get(move.toPlanet);
				} else {
					planetMoves = new ArrayList<Move>();
				}
				planetMoves.add(move);
				planetToMoves.put(move.toPlanet, planetMoves);
			}
		}

		float delay = 0.0f;
		for (Map.Entry<String, List<Move>> movesByPlanet : planetToMoves.entrySet()) {
			final PlanetButton planetButton = planetButtons.get(movesByPlanet.getKey());

			boolean anyMoveExecuted = false;
			for (Move move : movesByPlanet.getValue()) {
				Image movetoDisplay = MoveFactory.createShipForDisplay(move.angleOfMovement(), move.previousPosition,
						resources, boardCalcs);

				boolean showMove = true;

				Point newShipPosition = MoveFactory.getShipPosition(movetoDisplay, move.currentPosition, boardCalcs);
				if (!roundHasAlreadyBeenAnimated()) {
					movetoDisplay.addAction(delay(delay, moveTo(newShipPosition.x, newShipPosition.y, 1.2f)));
					delay += 0.5f;
				} else {
					movetoDisplay.setPosition(newShipPosition.x, newShipPosition.y);
				}

				Color color = Constants.Colors.USER_SHIP_FILL;
				if (!move.belongsToPlayer(GameLoop.USER)) {
					color = Constants.Colors.ENEMY_SHIP_FILL;
				}

				movetoDisplay.setColor(color);

				if (move.executed && !roundHasAlreadyBeenAnimated()) {
					movetoDisplay.addAction(scaleTo(0, 0, 0.8f + delay));
					addExplosion(move, 0.8f + delay, color);
					anyMoveExecuted = true;
				} else if (move.executed && roundHasAlreadyBeenAnimated()) {
					showMove = false;
				}
				movetoDisplay.setTouchable(Touchable.disabled);

				if (showMove) {
					boardTable.addActor(movetoDisplay);
				}
			}

			if (anyMoveExecuted) {
				delay += 0.8;
				planetButton.addAction(delay(delay, run(new Runnable() {
					@Override
					public void run() {
						planetButton.setShipCount(planetButton.planet.numberOfShips);
					}
				})));
				delay += 0.5;
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

			boardTable.addActor(particle);
		}
	}

	private void createMoveHud() {
		moveHud = new MoveHud(resources, gameBoard, screenCalcs.getBottomHudBounds().size.width,
				screenCalcs.getBottomHudBounds().size.height);

		for (Move move : gameBoard.movesInProgress) {
			if (move.belongsToPlayer(GameLoop.USER)) {
				moveHud.addMove(move);
			}
		}

		moveHud.addListener(new MoveListener() {

			@Override
			protected void performMove(int oldShipsToSend, Move move) {
				if (overlay != null) {
					overlay.remove();
				}

				highlight(move);
			}

			@Override
			public void sendMove(List<Move> moves) {
				overlay = new TextOverlay("Uploading ship movements", resources);
				stage.addActor(overlay);
				UIConnectionWrapper.performMoves(new UpdateBoardScreenResultHandler("Could not send moves"),
						gameBoard.id, moves, inProgressHarvest);
			}
		});

		stage.addActor(moveHud);
	}

	private void highlight(final Move move) {
		if (overlay != null && overlay instanceof HighlightOverlay) {
			((HighlightOverlay) overlay).hide();
		}
		overlay = (new HighlightOverlay(stage, gameBoard, moveHud, resources, screenCalcs, boardCalcs) {
			@Override
			public void onClose() {
				clearTouchedPlanets();
			}

			@Override
			public void onCancel() {
				deleteMove(move);
			}

			@Override
			public void onCreateMove(int oldShipsToSend, Move move) {
				createNewMove(oldShipsToSend, move);
			}
		}).add(move).focus(move);
	}

	private void highlight(Planet fromPlanet, Planet toPlanet) {
		List<Planet> planets = new ArrayList<Planet>();
		planets.add(fromPlanet);
		planets.add(toPlanet);
		Move fakeMove = MoveFactory.createMove(planets, 0, gameBoard.roundInformation.currentRound);

		highlight(fakeMove);
	}

	private void highlight(final Planet planet) {
		if (overlay != null && overlay instanceof HighlightOverlay) {
			((HighlightOverlay) overlay).hide();
		}
		overlay = (new HighlightOverlay(stage, gameBoard, moveHud, resources, screenCalcs, boardCalcs) {
			@Override
			public void onClose() {
				clearTouchedPlanets();
				clearMoveActions(planet);
			}
		}).add(planet).focus(planet);
	}

	private ClickListener clearPlanetListener() {
		return new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (!event.isStopped()) {
					clearTouchedPlanets();
				}
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
			line.setY(yOffset * i);
			line.setHeight(Gdx.graphics.getHeight() * 0.004f);
			boardTable.addActor(line);

		}

		for (int i = 1; i < gameBoard.widthInTiles; i++) {
			Line horizontalLine = new Line(grey, Gdx.graphics.getWidth() * 0.006f, lineRegion);
			horizontalLine.setY(0);
			horizontalLine.setX(xOffset * i);
			horizontalLine.setHeight(boardTable.getHeight());
			boardTable.addActor(horizontalLine);
		}
	}

	private void createPlanets() {
		for (final Planet planet : gameBoard.planets) {
			final PlanetButton planetButton = PlanetButtonFactory.createPlanetButton(planet, gameBoard,
					roundHasAlreadyBeenAnimated(), boardCalcs, resources);

			planetButton.addListener(new ClickListener() {

				@Override
				public void clicked(InputEvent event, float x, float y) {
					planetButton.addAction(forever(sequence(color(new Color(0, 0, 0, 0.4f), 0.7f),
							color(planet.getColor(), 0.5f))));
					if (touchedPlanets.size() < 2) {
						touchedPlanets.add(planet);
						renderDialog();
					} else {
						clearTouchedPlanets();
					}

					event.stop();
				}
			});
			boardTable.addActor(planetButton);
			planetButtons.put(planet.name, planetButton);

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
		boardTable.addActor(moon);
	}

	private void renderDialog() {
		if (touchedPlanets.size() > 1) {
			if (GameLoop.USER.hasMoved(gameBoard)) {
				if (planetsAreTheSame(touchedPlanets)) {
					highlight(touchedPlanets.get(0));
				} else {
					focusNewPlanet();
				}
			} else {
				if (planetsAreTheSame(touchedPlanets)) {
					highlight(touchedPlanets.get(0));
				} else if (!userPlanetInSelection()) {
					focusNewPlanet();
				} else {
					Planet userPlanet = touchedUserPlanet(touchedPlanets);
					Planet otherPlanet = otherPlanet(touchedPlanets, userPlanet);
					highlight(userPlanet, otherPlanet);
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

	private void focusNewPlanet() {
		clearMoveActions(touchedPlanets.get(0));
		Planet toKeep = touchedPlanets.get(1);
		touchedPlanets.clear();
		touchedPlanets.add(toKeep);
	}

	private boolean planetsAreTheSame(List<Planet> touchedPlanets2) {
		return touchedPlanets2.get(0).name.equals(touchedPlanets2.get(1).name);
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

	private void clearMoveActions(Planet planet) {
		planetButtons.get(planet.name).clearActions();
		planetButtons.get(planet.name).addAction(Actions.color(planet.getColor(), 0.4f));
	}

	private void createNewMove(int oldShipsToSend, Move newMove) {
		clearTouchedPlanets();

		if (!gameBoard.movesInProgress.contains(newMove)) {
			gameBoard.movesInProgress.add(newMove);
		}

		PlanetButton button = planetButtons.get(newMove.fromPlanet);
		button.setShipCount(button.getShipCount() - newMove.shipsToMove + oldShipsToSend);

		moveHud.addMove(newMove);

		clearMoveActions(newMove.fromPlanet(gameBoard.planets));
		clearMoveActions(newMove.toPlanet(gameBoard.planets));
	}

	private void deleteMove(Move move) {
		clearTouchedPlanets();

		PlanetButton button = planetButtons.get(move.fromPlanet);
		button.setShipCount(button.getShipCount() + move.shipsToMove);

		gameBoard.movesInProgress.remove(move);
		moveHud.removeMove(move);

		clearMoveActions(move.fromPlanet(gameBoard.planets));
		clearMoveActions(move.toPlanet(gameBoard.planets));
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

		// renderHarvest();

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
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
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
		moveHud = null;
	}

	public MenuScreenContainer getPreviousScreen() {
		return previousScreen;
	}

	public void setPreviousScreen(MenuScreenContainer previousScreen) {
		this.previousScreen = previousScreen;
	}

	public static class Labels {
		public static String waitingLabel(String social) {
			if (social != null && !social.isEmpty()) {
				return "[waiting for " + social + "]";
			}
			return "[Awaiting enemy]";
		}
	}
}

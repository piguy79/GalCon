package com.railwaygames.solarsmash.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.railwaygames.solarsmash.Constants.GALCON_PREFS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.ExternalActionWrapper;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.ScreenFeedback;
import com.railwaygames.solarsmash.UIConnectionWrapper;
import com.railwaygames.solarsmash.config.ConfigResolver;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.BaseResult;
import com.railwaygames.solarsmash.model.Bounds;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.HarvestMove;
import com.railwaygames.solarsmash.model.Move;
import com.railwaygames.solarsmash.model.Planet;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.model.Size;
import com.railwaygames.solarsmash.model.Social;
import com.railwaygames.solarsmash.model.factory.MoveFactory;
import com.railwaygames.solarsmash.model.factory.PlanetButtonFactory;
import com.railwaygames.solarsmash.screen.event.AboutEvent;
import com.railwaygames.solarsmash.screen.event.CancelDialogEvent;
import com.railwaygames.solarsmash.screen.event.CancelGameEvent;
import com.railwaygames.solarsmash.screen.event.ClaimVictoryEventListener;
import com.railwaygames.solarsmash.screen.event.GameReturnEventListener;
import com.railwaygames.solarsmash.screen.event.HarvestEvent;
import com.railwaygames.solarsmash.screen.event.MoveListener;
import com.railwaygames.solarsmash.screen.event.OKDialogEvent;
import com.railwaygames.solarsmash.screen.event.RefreshEvent;
import com.railwaygames.solarsmash.screen.event.ResignEvent;
import com.railwaygames.solarsmash.screen.event.TransitionEventListener;
import com.railwaygames.solarsmash.screen.level.LevelManager;
import com.railwaygames.solarsmash.screen.overlay.ClaimOverlay;
import com.railwaygames.solarsmash.screen.overlay.DismissableOverlay;
import com.railwaygames.solarsmash.screen.overlay.EndGameOverlay;
import com.railwaygames.solarsmash.screen.overlay.HighlightOverlay;
import com.railwaygames.solarsmash.screen.overlay.LevelUpOverlay;
import com.railwaygames.solarsmash.screen.overlay.LoserEndGameOverlay;
import com.railwaygames.solarsmash.screen.overlay.Overlay;
import com.railwaygames.solarsmash.screen.overlay.TextOverlay;
import com.railwaygames.solarsmash.screen.overlay.WinningEndGameOverlay;
import com.railwaygames.solarsmash.screen.ship.selection.BoardScreenOptionsDialog;
import com.railwaygames.solarsmash.screen.ship.selection.HarvestDialog;
import com.railwaygames.solarsmash.screen.widget.Line;
import com.railwaygames.solarsmash.screen.widget.Moon;
import com.railwaygames.solarsmash.screen.widget.PlanetButton;

public class BoardScreen implements ScreenFeedback {

	private int roundAnimated = -2;
	private GameBoard gameBoard;

	public List<Planet> touchedPlanets = new ArrayList<Planet>(2);
	List<HarvestMove> inProgressHarvest = new ArrayList<HarvestMove>();

	private String returnCode = null;

	private Stage stage;

	private Group boardTable;
	private MoveHud moveHud;
	private BoardScreenPlayerHud playerHud;

	private boolean claimShown = false;

	private List<Moon> moons = new ArrayList<Moon>();

	private Map<String, PlanetButton> planetButtons = new HashMap<String, PlanetButton>();

	private MenuScreenContainer previousScreen;

	private Overlay overlay;

	private Resources resources = null;

	/**
	 * Stores and pre-calculates common sizes and ratios of elements on the
	 * board screen.
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
			minPlanetRadius = (int) (largest * 0.6f);
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
	}

	public void setGameBoard(GameBoard gameBoard) {
		stage = new Stage();
		if (maxWidth == 0) {
			maxWidth = Gdx.graphics.getWidth();
			maxHeight = Gdx.graphics.getHeight();
		}

		clearTouchedPlanets();
		inProgressHarvest.clear();
		moons.clear();

		this.gameBoard = gameBoard;

		planetButtons.clear();
		if (moveHud != null) {
			moveHud.removeMoves();
		}

		planetTargetIcons.clear();
		planetTargetCount.clear();

		createLayout();

		// final Preferences prefs =
		// Gdx.app.getPreferences(Constants.GALCON_PREFS);
		// if (!prefs.getBoolean(Constants.Tutorial.OVERVIEW, false)) {
		// overlay = (new HighlightOverlay(stage, gameBoard, moveHud, resources,
		// screenCalcs, boardCalcs) {
		//
		// @Override
		// public void onClose() {
		// prefs.putBoolean(Constants.Tutorial.OVERVIEW, true);
		// prefs.flush();
		// beginOverlay();
		// }
		// }).focus(Constants.Tutorial.OVERVIEW);
		// } else {
		beginOverlay();
		// }

		stage.addListener(createHarvestListener());
	}

	private void beginOverlay() {
		if (!GameLoop.USER.hasMoved(gameBoard)) {
			showAd();

			if (LevelManager.shouldShowLevelUp(findPlayer(GameLoop.USER.handle))) {
				final LevelUpOverlay levelUp = new LevelUpOverlay(resources, findPlayer(GameLoop.USER.handle));
				levelUp.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						LevelManager.storeLevel(findPlayer(GameLoop.USER.handle));
						levelUp.remove();
						showRoundInfo();
					}
				});

				stage.addActor(levelUp);
			} else {
				showRoundInfo();
			}
		} else {
			beginEndRoundInfo();
		}
	}

	private void showAd() {
		Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
		String lastAdShownTime = prefs.getString(Constants.LAST_AD_SHOWN);
		Long currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();

		if (lastAdShownTime == null || lastAdShownTime.isEmpty()) {
			prefs.putString(Constants.LAST_AD_SHOWN, currentTime.toString());
			prefs.flush();
		} else if (adTimeoutIsPassed(lastAdShownTime, currentTime)) {
			ExternalActionWrapper.showAd();
			prefs.putString(Constants.LAST_AD_SHOWN, currentTime.toString());
			prefs.flush();
		}

	}

	private boolean adTimeoutIsPassed(String lastAdShownTime, Long currentTime) {
		return (currentTime - Long.parseLong(lastAdShownTime)) > Long.parseLong(ConfigResolver
				.getByConfigKey(Constants.Config.AD_TIMEOUT));
	}

	private Player findPlayer(String handle) {
		for (Player player : gameBoard.players) {
			if (player.handle.equals(handle)) {
				return player;
			}
		}

		return null;
	}

	private void showRoundInfo() {
		overlay = (new HighlightOverlay(stage, gameBoard, moveHud, resources, screenCalcs, boardCalcs) {

			@Override
			public void onClose() {
				beginEndRoundInfo();
			}
		}).focus(gameBoard.roundInformation);
	}

	private void beginEndRoundInfo() {
		beginShipMovements();
		if (!claimShown && gameBoard.isClaimAvailable()) {
			showClaimOverlay();
		} else {
			createEndGameOverlay();
		}
	}

	private void showClaimOverlay() {
		claimShown = true;
		final ClaimOverlay claimOverlay = new ClaimOverlay(resources, gameBoard);

		claimOverlay.addListener(new ClaimVictoryEventListener() {
			@Override
			public void claimFailed() {
				claimOverlay.remove();
				final Overlay claimFailOverlay = new DismissableOverlay(resources, new TextOverlay(
						"Unable to claim victory", resources), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						overlay = new TextOverlay("Refreshing", resources);
						stage.addActor(overlay);
						UIConnectionWrapper.findGameById(new UpdateBoardScreenResultHandler("Could not refresh"),
								gameBoard.id, GameLoop.USER.handle);
					}
				});
				stage.addActor(claimFailOverlay);
			}

			@Override
			public void claimSuccess() {
				claimOverlay.remove();
				overlay = new TextOverlay("Refreshing", resources);
				stage.addActor(overlay);
				UIConnectionWrapper.findGameById(new UpdateBoardScreenResultHandler("Could not refresh"), gameBoard.id,
						GameLoop.USER.handle);
			}
		});
		stage.addActor(claimOverlay);
	}

	private void beginShipMovements() {
		createMoves();
	}

	private void createLayout() {
		AtlasRegion bg = resources.levelAtlas.findRegion("" + gameBoard.map);

		screenCalcs = new ScreenCalculations(maxWidth, maxHeight);
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
		createPlanets();
		createPlayerHud();
		createPlanetIcons();

		Gdx.input.setInputProcessor(stage);
	}

	private void createEndGameOverlay() {
		if (gameBoard.hasWinner()) {
			EndGameOverlay endGameOverlay;
			if (gameBoard.endGameInformation.winnerHandle.equals(GameLoop.USER.handle)) {
				endGameOverlay = new WinningEndGameOverlay(resources, gameBoard);
			} else {
				endGameOverlay = new LoserEndGameOverlay(resources, gameBoard);
			}

			endGameOverlay.addListener(new TransitionEventListener() {
				@Override
				public void transition(String action) {
					stage.dispose();
					returnCode = action;
				}
			});

			endGameOverlay.addListener(new GameReturnEventListener() {
				@Override
				public void gameFound(String gameId) {
					UIConnectionWrapper.findGameById(new UpdateBoardScreenResultHandler("Could not refresh"), gameId,
							GameLoop.USER.handle);
				}
			});
			stage.addActor(endGameOverlay);
		} else if (gameBoard.social != null && gameBoard.social.status.equals("DECLINED")) {
			TextOverlay overlay = showEndDialog(gameBoard.social.invitee
					+ " has declined to play.\nYour coin has been returned.");
			stage.addActor(overlay);
		}
	}

	private TextOverlay showEndDialog(String endGameMessage) {
		TextOverlay overlay = new TextOverlay(endGameMessage, resources);
		overlay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				stage.dispose();
				returnCode = Action.BACK;
			}
		});
		return overlay;
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
					BoardScreenOptionsDialog dialog = new BoardScreenOptionsDialog(gameBoard, resources,
							maxWidth * 0.8f, maxHeight * 0.4f, stage);
					float dialogY = maxHeight - (dialog.getHeight() + (dialog.getHeight() * 0.5f));
					dialog.setX(-dialog.getWidth());
					dialog.setY(dialogY);
					stage.addActor(dialog);
					dialog.show(new Point(maxWidth * 0.1f, dialogY));

					dialog.addListener(new EventListener() {
						@Override
						public boolean handle(Event event) {
							if (event instanceof ResignEvent) {
								overlay = new TextOverlay("Resigning Game", resources);
								stage.addActor(overlay);
								UIConnectionWrapper.resignGame(new UpdateBoardScreenResultHandler("Could not resign"),
										gameBoard.id, GameLoop.USER.handle);
								return true;
							} else if (event instanceof RefreshEvent) {
								overlay = new TextOverlay("Refreshing", resources);
								stage.addActor(overlay);
								UIConnectionWrapper.findGameById(
										new UpdateBoardScreenResultHandler("Could not refresh"), gameBoard.id,
										GameLoop.USER.handle);
								return true;
							} else if (event instanceof AboutEvent) {
								final Overlay ovrlay = new DismissableOverlay(
										resources,
										new TextOverlay(
												"About\n\n"
														+ "This project heavily utilizes libGDX and RoboVM.  Thanks to both amazing projects.\n\n"
														+ "All space images are courtesy NASA/JPL-Caltech.", resources),
										null);
								stage.addActor(ovrlay);
								return true;

							} else if (event instanceof CancelGameEvent) {
								overlay = new TextOverlay("Cancelling Game", resources);
								stage.addActor(overlay);
								UIConnectionWrapper.cancelGame(new UIConnectionResultCallback<BaseResult>() {
									public void onConnectionResult(BaseResult result) {
										if (result.success) {
											stage.dispose();
											returnCode = Action.BACK;
										} else {
											overlay = new DismissableOverlay(resources, new TextOverlay(
													"Unable to cancel game", resources), new ClickListener() {
												public void clicked(InputEvent event, float x, float y) {
													UIConnectionWrapper.findGameById(
															new UpdateBoardScreenResultHandler("Could not refresh"),
															gameBoard.id, GameLoop.USER.handle);
													overlay.remove();
												};
											});
											stage.addActor(overlay);
										}
									};

									public void onConnectionError(String msg) {
										overlay.remove();
									};
								}, GameLoop.USER.handle, gameBoard.id);
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
		for (Move move : gameBoard.movesInProgress) {
			if (move.executed || !move.belongsToPlayer(GameLoop.USER)) {
				continue;
			}
			Image movetoDisplay = MoveFactory.createShipForDisplay(move.angleOfMovement(gameBoard),
					move.previousPosition, resources, boardCalcs);

			Point newShipPosition = MoveFactory.getShipPosition(movetoDisplay, move.currentPosition, boardCalcs);
			if (!roundHasAlreadyBeenAnimated()) {
				movetoDisplay.addAction(delay(0.0f, moveTo(newShipPosition.x, newShipPosition.y, 1.2f)));
			} else {
				movetoDisplay.setPosition(newShipPosition.x, newShipPosition.y);
			}

			Color color = Constants.Colors.USER_SHIP_FILL;
			if (!move.belongsToPlayer(GameLoop.USER)) {
				color = Constants.Colors.ENEMY_SHIP_FILL;
			}

			movetoDisplay.setColor(color);
			movetoDisplay.setTouchable(Touchable.disabled);

			boardTable.addActor(movetoDisplay);
		}

		roundAnimated = gameBoard.roundInformation.round;
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

	private EventListener createHarvestListener() {
		return new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (!(event instanceof HarvestEvent)) {
					return false;
				}

				final HarvestEvent hEvent = (HarvestEvent) event;

				HarvestDialog dialog = new HarvestDialog(gameBoard, resources, maxWidth * 0.8f, maxHeight * 0.3f, stage);
				float dialogY = maxHeight - (dialog.getHeight() + (dialog.getHeight() * 0.5f));
				dialog.setX(-dialog.getWidth());
				dialog.setY(dialogY);
				stage.addActor(dialog);
				dialog.show(new Point(maxWidth * 0.1f, dialogY));

				dialog.addListener(new EventListener() {
					@Override
					public boolean handle(Event event) {
						if (event instanceof OKDialogEvent) {
							overlay = new DismissableOverlay(resources, new TextOverlay(
									"Harvest will begin\nthe next round", resources), null);
							stage.addActor(overlay);

							boolean harvestExists = false;
							for (HarvestMove harvestMove : inProgressHarvest) {
								if (harvestMove.planet.equals(hEvent.getPlanetToHarvest())) {
									harvestExists = true;
								}
							}
							if (!harvestExists) {
								inProgressHarvest.add(MoveFactory.createHarvestMove(hEvent.getPlanetToHarvest()));
							}
							return true;
						} else if (event instanceof CancelDialogEvent) {

						}
						return false;
					}
				});

				return true;
			}
		};
	}

	private void highlight(Planet fromPlanet, Planet toPlanet) {
		List<Planet> planets = new ArrayList<Planet>();
		planets.add(fromPlanet);
		planets.add(toPlanet);
		Move fakeMove = MoveFactory.createMove(planets, 0, gameBoard.roundInformation.round);

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
		grey.a = 1.0f;
		TextureRegion lineRegion = resources.gameBoardAtlas.findRegion("line");

		Group grids = new Group();
		grids.setBounds(0, 0, boardTable.getWidth(), boardTable.getHeight());

		for (int i = 1; i < gameBoard.heightInTiles; i++) {
			Line line = new Line(grey, maxWidth, lineRegion);
			line.setY(yOffset * i);
			line.setHeight(maxHeight * 0.004f);
			grids.addActor(line);
		}

		for (int i = 1; i < gameBoard.widthInTiles; i++) {
			Line horizontalLine = new Line(grey, maxWidth * 0.006f, lineRegion);
			horizontalLine.setY(0);
			horizontalLine.setX(xOffset * i);
			horizontalLine.setHeight(boardTable.getHeight());
			grids.addActor(horizontalLine);
		}

		boardTable.addActor(grids);
	}

	private void createPlanetIcons() {
		Set<String> targettedPlanets = new HashSet<String>();
		for (Move move : gameBoard.movesInProgress) {
			if (move.belongsToPlayer(GameLoop.USER) && !move.executed) {
				targettedPlanets.add(move.to);
			}
		}

		for (String targettedPlanet : targettedPlanets) {
			final PlanetButton planetButton = planetButtons.get(targettedPlanet);
			createPlanetTarget(planetButton);
		}
	}

	private Map<String, Integer> planetTargetCount = new HashMap<String, Integer>();
	private Map<PlanetButton, Image> planetTargetIcons = new HashMap<PlanetButton, Image>();

	private void createPlanetTarget(PlanetButton button) {
		Integer count = planetTargetCount.get(button.planet.name);
		if (count == null) {
			count = Integer.valueOf(1);
		} else {
			count = count + 1;
		}
		planetTargetCount.put(button.planet.name, count);

		if (count != 1) {
			return;
		}

		Image targetImage;
		if (button.planet.isOwnedBy(GameLoop.USER.handle)) {
			targetImage = new Image(resources.skin, "transfer");
		} else {
			targetImage = new Image(resources.skin, "crosshairs");
		}
		Point center = button.centerPoint();

		Size tileSize = boardCalcs.getTileSize();
		float size = tileSize.width * 0.2f;

		targetImage.setBounds(center.x + tileSize.width * 0.5f - size, center.y + tileSize.height * 0.5f - size, size,
				size);
		targetImage.setColor(new Color(1.0f, 1.0f, 1.0f, 0.8f));

		boardTable.addActor(targetImage);
		planetTargetIcons.put(button, targetImage);
	}

	private void createPlanets() {
		for (final Planet planet : gameBoard.planets) {
			final PlanetButton planetButton = PlanetButtonFactory.createPlanetButton(planet, gameBoard, true,
					boardCalcs, resources);

			planetButton.addListener(new ClickListener() {

				@Override
				public void clicked(InputEvent event, float x, float y) {
					planetButton.addGlow();
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
				createMoon(planetButton);
			}
		}
	}

	private void createMoon(PlanetButton planetButton) {
		final Moon moon = PlanetButtonFactory.createMoon(resources, gameBoard, planetButton, boardCalcs, moons);

		float relativeX = planetButton.centerPoint().x - (moon.getWidth() / 2);
		float relativeY = planetButton.centerPoint().y - (moon.getHeight() / 2);

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
			if (planet.isOwnedBy(GameLoop.USER.handle)) {
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
		planetButtons.get(planet.name).removeGlow();
	}

	private void createNewMove(int oldShipsToSend, Move newMove) {
		clearTouchedPlanets();

		if (!gameBoard.movesInProgress.contains(newMove)) {
			gameBoard.movesInProgress.add(newMove);
			createPlanetTarget(planetButtons.get(newMove.to));
		}

		PlanetButton button = planetButtons.get(newMove.from);
		button.setShipCount(button.getShipCount() - newMove.shipsToMove + oldShipsToSend);

		moveHud.addMove(newMove);

		clearMoveActions(newMove.fromPlanet(gameBoard.planets));
		clearMoveActions(newMove.toPlanet(gameBoard.planets));
	}

	private void deleteMove(Move move) {
		clearTouchedPlanets();

		PlanetButton button = planetButtons.get(move.from);
		button.setShipCount(button.getShipCount() + move.shipsToMove);

		gameBoard.movesInProgress.remove(move);
		moveHud.removeMove(move);

		clearMoveActions(move.fromPlanet(gameBoard.planets));
		clearMoveActions(move.toPlanet(gameBoard.planets));

		if (planetTargetCount.containsKey(move.to)) {
			Integer count = planetTargetCount.get(move.to) - 1;
			planetTargetCount.put(move.to, count);

			if (count == 0) {
				Image targetIcon = planetTargetIcons.remove(planetButtons.get(move.to));
				targetIcon.remove();
			}
		}
	}

	private boolean roundHasAlreadyBeenAnimated() {
		return roundAnimated == gameBoard.roundInformation.round;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (gameBoard != null) {
			renderMoons();
		}

		if (stage != null) {
			stage.act(delta);
			stage.draw();
		}
	}

	private void renderMoons() {
		for (Moon moon : moons) {
			Point newPosition = PlanetButtonFactory.findMoonPosition(moon, boardCalcs);
			if (newPosition != null) {
				moon.updateLocation(boardTable, boardCalcs, newPosition);
			}
		}
	}

	private void clearTouchedPlanets() {
		for (Planet planet : touchedPlanets) {
			PlanetButton button = planetButtons.get(planet.name);
			button.removeGlow();
		}

		touchedPlanets.clear();

		if (gameBoard == null || gameBoard.planets == null) {
			return;
		}
		for (Planet planet : gameBoard.planets) {
			planet.touched = false;
		}
	}

	private int maxWidth = 0;
	private int maxHeight = 0;

	@Override
	public void resize(int width, int height) {
		if (stage != null) {
			if (height > maxHeight) {
				maxHeight = height;
			}
			if (width > maxWidth) {
				maxWidth = width;
			}
			stage.getViewport().update(maxWidth, maxHeight, true);
		}
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
			overlay.remove();

			overlay = new DismissableOverlay(resources, new TextOverlay(errorMessage, resources), null);
			stage.addActor(overlay);
		}
	}

	@Override
	public void resetState() {
		returnCode = null;
		roundAnimated = -2;
		clearTouchedPlanets();

		gameBoard = null;
		moveHud = null;
		claimShown = false;
	}

	public MenuScreenContainer getPreviousScreen() {
		return previousScreen;
	}

	public void setPreviousScreen(MenuScreenContainer previousScreen) {
		this.previousScreen = previousScreen;
	}

	public static class Labels {
		public static String waitingLabel(Social social) {
			if (social != null && !social.invitee.isEmpty()) {
				return "[" + social.invitee + "]";
			}
			return "[Awaiting enemy]";
		}
	}

	public void setConnectionError(String error) {
		if (stage == null) {
			stage = new Stage();
		}
		overlay = new DismissableOverlay(resources, new TextOverlay(error, resources), new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				returnCode = Action.BACK;
			}
		});
		stage.addActor(overlay);
	}

	@Override
	public void refresh() {

	}
}

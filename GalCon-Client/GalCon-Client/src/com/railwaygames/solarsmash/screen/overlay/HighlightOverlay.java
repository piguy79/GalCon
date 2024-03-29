package com.railwaygames.solarsmash.screen.overlay;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static java.lang.Math.floor;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.math.GalConMath;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.GameBoard.Record;
import com.railwaygames.solarsmash.model.Move;
import com.railwaygames.solarsmash.model.Planet;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.model.RoundInformation;
import com.railwaygames.solarsmash.model.Size;
import com.railwaygames.solarsmash.model.factory.MoveFactory;
import com.railwaygames.solarsmash.model.factory.PlanetButtonFactory;
import com.railwaygames.solarsmash.screen.BoardScreen.BoardCalculations;
import com.railwaygames.solarsmash.screen.BoardScreen.ScreenCalculations;
import com.railwaygames.solarsmash.screen.MoveHud;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.event.HarvestEvent;
import com.railwaygames.solarsmash.screen.event.MoveListener;
import com.railwaygames.solarsmash.screen.event.NextPageEvent;
import com.railwaygames.solarsmash.screen.hud.BasicTextHud;
import com.railwaygames.solarsmash.screen.hud.MultiPageBottomHud;
import com.railwaygames.solarsmash.screen.hud.PlanetInfoHud;
import com.railwaygames.solarsmash.screen.hud.RoundInformationTopHud;
import com.railwaygames.solarsmash.screen.hud.ShipSelectionHud;
import com.railwaygames.solarsmash.screen.hud.SingleMoveInfoHud;
import com.railwaygames.solarsmash.screen.tutorial.Overview;
import com.railwaygames.solarsmash.screen.tutorial.Tutorial;
import com.railwaygames.solarsmash.screen.widget.Moon;
import com.railwaygames.solarsmash.screen.widget.PlanetButton;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public abstract class HighlightOverlay extends Overlay {
	private Huds huds;
	private ScreenCalculations screenCalcs;
	private BoardCalculations boardCalcs;
	private GameBoard gameBoard;
	private MoveHud moveHud;
	private List<String> planetsConquered;

	private ClickListener defaultHideListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			hide();
		}
	};

	private Resources resources;

	public HighlightOverlay(Stage stage, GameBoard gameBoard, MoveHud moveHud, Resources resources,
			ScreenCalculations screenCalcs, BoardCalculations boardCalcs) {
		super(resources);
		this.screenCalcs = screenCalcs;
		this.boardCalcs = boardCalcs;
		this.gameBoard = gameBoard;
		this.moveHud = moveHud;
		this.resources = resources;
		this.planetsConquered = new ArrayList<String>();

		screenCalcs.getBoardBounds().applyBounds(this);
		stage.addActor(this);

		addListener(defaultHideListener);
	}

	public HighlightOverlay focus(Move move) {
		huds = new MoveHuds();
		huds.createTopHud(move);
		huds.createBottomHud();
		huds.show();

		return this;
	}

	public HighlightOverlay focus(RoundInformation roundInformation) {
		// we want users to use the 'go' button instead
		this.removeListener(defaultHideListener);

		huds = new RoundInformationHuds();
		huds.createTopHud(null);
		huds.createBottomHud();
		huds.show();

		String playerHandle1 = gameBoard.getUser().handle;
		String playerHandle2 = gameBoard.getEnemy().handle.replace("[", "").replace("]", "");

		if (roundInformation.round == 0) {
			createPlayerLabels(playerHandle1, 0.7f, Align.left);
			createPlayerLabels(playerHandle2, 0.3f, Align.right);

			Integer p1Wins = gameBoard.handleToVictoriesVsOpponent.get(playerHandle1);
			p1Wins = p1Wins == null ? 0 : p1Wins;
			Integer p2Wins = gameBoard.handleToVictoriesVsOpponent.get(playerHandle2);
			p2Wins = p2Wins == null ? 0 : p2Wins;
			{
				ShaderLabel lbl = new ShaderLabel(resources.fontShader, "Head to Head Record", resources.skin,
						Constants.UI.SMALL_FONT, Color.WHITE);
				lbl.setWidth(Gdx.graphics.getWidth());
				lbl.setX(0);
				lbl.setY(Gdx.graphics.getHeight() * 0.45f - lbl.getHeight() * 0.5f);
				lbl.setAlignment(Align.center, Align.center);
				lbl.setTouchable(Touchable.disabled);
				lbl.setColor(Color.CLEAR);
				lbl.addAction(color(Color.WHITE, 0.66f));
				addActor(lbl);
			}
			{
				ShaderLabel lbl = new ShaderLabel(resources.fontShader, winLossRecordString(p1Wins, p2Wins),
						resources.skin, Constants.UI.MEDIUM_LARGE_FONT, Color.WHITE);
				lbl.setWidth(Gdx.graphics.getWidth());
				lbl.setX(0);
				lbl.setY(Gdx.graphics.getHeight() * 0.42f - lbl.getHeight() * 0.5f);
				lbl.setAlignment(Align.center, Align.center);
				lbl.setTouchable(Touchable.disabled);
				lbl.setColor(Color.CLEAR);
				lbl.addAction(color(Color.WHITE, 0.66f));
				addActor(lbl);
			}
		} else {
			{
				ShaderLabel lbl = new ShaderLabel(resources.fontShader, "Round", resources.skin,
						Constants.UI.LARGE_FONT, Color.WHITE);
				lbl.setWidth(Gdx.graphics.getWidth());
				lbl.setX(0);
				lbl.setY(Gdx.graphics.getHeight() * 0.7f - lbl.getHeight() * 0.5f);
				lbl.setAlignment(Align.center, Align.center);
				lbl.setTouchable(Touchable.disabled);
				lbl.setColor(Color.CLEAR);
				lbl.addAction(color(Color.WHITE, 0.66f));
				addActor(lbl);
			}
			{
				ShaderLabel lbl = new ShaderLabel(resources.fontShader, "" + (roundInformation.round + 1),
						resources.skin, Constants.UI.LARGE_FONT, Color.WHITE);
				lbl.setWidth(Gdx.graphics.getWidth());
				lbl.setX(0);
				lbl.setY(Gdx.graphics.getHeight() * 0.6f - lbl.getHeight() * 0.5f);
				lbl.setAlignment(Align.center, Align.center);
				lbl.setTouchable(Touchable.disabled);
				lbl.setColor(Color.CLEAR);
				lbl.addAction(delay(0.33f, color(Color.WHITE, 0.66f)));
				addActor(lbl);
			}
		}

		return this;
	}

	private void createPlayerLabels(String handle, float y, int align) {
		Record last10Record = gameBoard.handleToVictoriesInLast10.get(handle);
		int wins = last10Record == null ? 0 : last10Record.wins;
		int losses = last10Record == null ? 0 : last10Record.losses;

		float margin = Gdx.graphics.getWidth() * 0.05f;
		float width = Gdx.graphics.getWidth() - 2.0f * margin;
		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, handle, resources.skin,
					Constants.UI.MEDIUM_LARGE_FONT, Color.WHITE);
			lbl.setWidth(width);
			lbl.setX(margin);
			lbl.setY(Gdx.graphics.getHeight() * y - lbl.getHeight() * 0.5f);
			lbl.setAlignment(align, align);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			if (handle.equals(GameLoop.getUser().handle)) {
				lbl.addAction(color(Constants.Colors.USER_SHIP_FILL, 0.66f));
			} else {
				lbl.addAction(color(Constants.Colors.ENEMY_SHIP_FILL, 0.66f));
			}
			addActor(lbl);
		}
		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, "Overall: " + extractWinLossRecord(handle),
					resources.skin, Constants.UI.SMALL_FONT, Color.WHITE);
			lbl.setWidth(width);
			lbl.setX(margin);
			lbl.setY(Gdx.graphics.getHeight() * (y - 0.06f) - lbl.getHeight() * 0.5f);
			lbl.setAlignment(align, align);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			lbl.addAction(color(Color.WHITE, 0.66f));
			addActor(lbl);
		}
		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, "Last 10: " + winLossRecordString(wins, losses),
					resources.skin, Constants.UI.SMALL_FONT, Color.WHITE);
			lbl.setWidth(width);
			lbl.setX(margin);
			lbl.setY(Gdx.graphics.getHeight() * (y - 0.09f) - lbl.getHeight() * 0.5f);
			lbl.setAlignment(align, align);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			lbl.addAction(color(Color.WHITE, 0.66f));
			addActor(lbl);
		}
	}

	private String extractWinLossRecord(String handle) {
		int losses = 0;
		int wins = 0;
		Record record = gameBoard.handleToOverallRecord.get(handle);
		if (record != null) {
			losses = record.losses;
			wins = record.wins;
		}

		return winLossRecordString(wins, losses);
	}

	private String winLossRecordString(int wins, int losses) {
		return "(" + wins + " - " + losses + ")";
	}

	public HighlightOverlay focus(String tutorial, String continuePoint) {
		// we want users to use the 'go' button instead
		this.removeListener(defaultHideListener);

		huds = new TutorialHuds(tutorial, continuePoint);
		huds.createTopHud(null);
		huds.createBottomHud();
		huds.show();

		this.backGround.setColor(Constants.Colors.OVERLAY_RED);

		return this;
	}

	public void hide() {
		huds.hide();
		remove();
		onClose("");
	}

	/**
	 * Called when the user dismisses the overlay
	 */
	public abstract void onClose(String msg);

	/**
	 * User has clicked the cancel button on the ship selection hud.
	 */
	public void onCancel() {

	}

	/**
	 * User has clicked the OK button on the ship selection hud.
	 */
	public void onCreateMove(int oldShipsToSend, Move move) {

	}

	public HighlightOverlay focus(Planet planet) {
		huds = new PlanetHuds();
		huds.createTopHud(planet);
		huds.show();

		return this;
	}

	public HighlightOverlay add(Move move) {
		Planet fromPlanet = gameBoard.getPlanet(move.from);
		Planet toPlanet = gameBoard.getPlanet(move.to);

		final PlanetButton toPlanetButton = PlanetButtonFactory.createPlanetButton(toPlanet, gameBoard, !move.executed,
				boardCalcs, resources);

		addActor(toPlanetButton);

		Point position = move.currentPosition;
		if (move.executed) {
			position = move.previousPosition;
		} else {
			highlightPath(fromPlanet, toPlanet);
			final PlanetButton fromPlanetButton = PlanetButtonFactory.createPlanetButton(fromPlanet, gameBoard, true,
					boardCalcs, resources);
			addActor(fromPlanetButton);
		}

		Image moveToDisplay = MoveFactory.createShipForDisplay(move.angleOfMovement(gameBoard), position, resources,
				boardCalcs);

		Color color = Constants.Colors.USER_SHIP_FILL;
		if (!move.belongsToPlayer(GameLoop.getUser())) {
			color = Constants.Colors.ENEMY_SHIP_FILL;
		}
		moveToDisplay.setColor(color);

		if (move.executed) {
			Point newShipPosition = MoveFactory.getShipPosition(moveToDisplay, move.currentPosition, boardCalcs);
			moveToDisplay.addAction(delay(0.0f, moveTo(newShipPosition.x, newShipPosition.y, 1.0f)));

			moveToDisplay.addAction(delay(0.25f, scaleTo(0, 0, 0.75f)));
			String previousOwner = toPlanetButton.planet.previousRoundOwner(gameBoard);
			toPlanetButton.showPlanetState(true, true);

			if (!previousOwner.equals(move.handle)) {
				addExplosion(false, move.shipsToMove, toPlanetButton.planet.position, 1.0f, color);
			}

			if (move.executed && !move.battleStats.previousPlanetOwner.equals(move.handle)
					&& toPlanetButton.planet.isOwnedBy(GameLoop.getUser().handle)
					&& toPlanetButton.planet.isOwnedBy(move.handle)
					&& !planetsConquered.contains(toPlanetButton.planet.name)) {
				planetsConquered.add(toPlanetButton.planet.name);
				addXpGainLabel(toPlanetButton.planet.position, toPlanetButton.planet.owner);
			}
		}

		addActor(moveToDisplay);

		return this;
	}

	private void addExplosion(boolean smallExplosion, int shipsToMove, Point position, float delay, Color color) {
		int minNumberOfParticles = 6;
		int maxNumberOfParticles = 15;
		float ratio = ((float) shipsToMove) / 30.0f;
		int numberOfParticles = min(maxNumberOfParticles, (int) floor(minNumberOfParticles
				+ (maxNumberOfParticles - minNumberOfParticles) * ratio));

		float tileWidth = boardCalcs.getTileSize().width;

		float circleInRadians = (float) Math.PI * 2.0f;
		float startRadius = tileWidth * 0.05f;
		float particleSize = tileWidth * 0.2f;
		float radiansBetweenParticles = circleInRadians / (float) numberOfParticles;

		if (smallExplosion) {
			particleSize *= 0.75f;
		}

		Point tileCenter = boardCalcs.tileCoordsToPixels(position);

		for (float currentAngle = 0; currentAngle < circleInRadians; currentAngle += radiansBetweenParticles) {
			Image particle = new Image(resources.skin, Constants.UI.EXPLOSION_PARTICLE);

			float yStartOffset = (float) MathUtils.sin(currentAngle) * startRadius;
			float xStartOffset = (float) MathUtils.cos(currentAngle) * startRadius;

			particle.setColor(Color.CLEAR);
			particle.setOrigin(particleSize * 0.5f, particleSize * 0.5f);
			particle.setBounds(tileCenter.x + xStartOffset, tileCenter.y + yStartOffset, particleSize, particleSize);
			particle.addAction(sequence(delay(delay), color(color)));
			particle.addAction(sequence(delay(delay),
					Actions.moveBy(xStartOffset * 12, yStartOffset * 12, 1.5f, Interpolation.circleOut)));
			particle.addAction(sequence(delay(delay + 0.1f), Actions.rotateBy(1000, 2.0f)));
			particle.addAction(sequence(delay(delay + 1.0f), alpha(0.0f, 1.5f)));

			addActor(particle);
		}
	}

	private void addXpGainLabel(Point position, String planetOwner) {

		Point tileCenter = boardCalcs.tileCoordsToPixels(position);
		float yIncrease = boardCalcs.getTileSize().height * 0.65f;

		if (position.y >= gameBoard.heightInTiles - 1.5) {
			yIncrease = yIncrease * -1f;
		}

		Color fontColorToUse = Color.RED;
		if (planetOwner.equals(GameLoop.getUser().handle)) {
			fontColorToUse = Color.GREEN;
		}

		String labelText = gameBoard.gameConfig.getValue(Constants.XP_FROM_PLANET_CAPTURE);
		final ShaderLabel label = new ShaderLabel(resources.fontShader, "+" + labelText + "xp", resources.skin,
				Constants.UI.DEFAULT_FONT, fontColorToUse);
		label.setX(tileCenter.x);
		label.setY(tileCenter.y);
		addActor(label);

		label.addAction(Actions.sequence(Actions.fadeOut(0.0f), Actions.delay(0.9f), Actions.fadeIn(0),
				Actions.moveBy(0, yIncrease, 1.2f), Actions.fadeOut(0.6f), Actions.run(new Runnable() {
					@Override
					public void run() {
						label.remove();
					}
				})));
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
			particle.rotateBy((float) Math.toDegrees(angle));
			particle.addAction(sequence(delay(startDelay),
					forever(sequence(color(Color.BLACK, 0.5f), color(Color.YELLOW, 0.5f)))));
			startDelay += 0.1f;

			addActor(particle);
		}
	}

	public HighlightOverlay add(Planet planet) {
		final PlanetButton planetButton = PlanetButtonFactory.createPlanetButton(planet, gameBoard, true, boardCalcs,
				resources);
		addActor(planetButton);

		if (planet.hasAbility()) {
			Moon moon = PlanetButtonFactory.createMoon(resources, gameBoard, planetButton, boardCalcs, null);
			moon.updateLocation(this, boardCalcs, new Point(0, 0));
			addActor(moon);
		}

		return this;
	}

	private interface Huds<T> {
		public void createTopHud(T object);

		public void createBottomHud();

		public void show();

		public void hide();
	}

	private void hide(final Actor top, final Actor bottom) {
		if (top != null) {
			top.addAction(sequence(moveTo(0, Gdx.graphics.getHeight(), 0.5f, Interpolation.circleOut),
					run(new Runnable() {
						@Override
						public void run() {
							top.remove();
						}
					})));
		}

		if (bottom != null) {
			bottom.addAction(sequence(
					moveTo(0, -screenCalcs.getBottomHudBounds().size.height, 0.5f, Interpolation.circleOut),
					run(new Runnable() {
						@Override
						public void run() {
							bottom.remove();
						}
					})));
		}
	}

	private class TutorialHuds implements Huds<String> {

		private Tutorial tutorial;
		private BasicTextHud topHud;
		private MultiPageBottomHud bottomHud;
		private int currentPage = 1;

		public TutorialHuds(String tutorialString, String continuePoint) {
			if (tutorialString.equals(Constants.Tutorial.OVERVIEW)) {
				this.tutorial = new Overview(resources, HighlightOverlay.this, !GameLoop.getUser().hasMoved(gameBoard));
			}

			currentPage = tutorial.getPage(continuePoint);
			this.tutorial.showPage(currentPage);
		}

		@Override
		public void createTopHud(String object) {
			Size size = screenCalcs.getTopHudBounds().size;
			topHud = new BasicTextHud(resources, size.width, size.height);
		}

		@Override
		public void createBottomHud() {
			Size size = screenCalcs.getBottomHudBounds().size;
			bottomHud = new MultiPageBottomHud(resources, size.width, size.height);
			bottomHud.changeButtonText("Next >");
			bottomHud.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					if (!(event instanceof NextPageEvent)) {
						return false;
					}

					String pauseEvent = tutorial.pauseEvent(currentPage);
					currentPage += 1;
					if (pauseEvent.equals("") && currentPage <= tutorial.getPageCount()) {
						tutorial.showPage(currentPage);
					} else {
						hide();
						remove();
						onClose(pauseEvent);
					}

					return true;
				}
			});
		}

		@Override
		public void show() {
			Point topOrigin = screenCalcs.getTopHudBounds().origin;
			Point bottomOrigin = screenCalcs.getBottomHudBounds().origin;

			topHud.setX(topOrigin.x);
			topHud.setY(topOrigin.y);
			bottomHud.setX(bottomOrigin.x);
			bottomHud.setY(bottomOrigin.y);

			getStage().addActor(bottomHud);
			getStage().addActor(topHud);
		}

		@Override
		public void hide() {
			HighlightOverlay.this.hide(topHud, bottomHud);
		}
	}

	private class RoundInformationHuds implements Huds<List<Move>> {

		private MultiPageBottomHud bottomHud;
		private RoundInformationTopHud topHud;

		private Map<String, List<Move>> planetToMoves;
		private Iterator<String> planetIterator;
		private Iterator<Planet> harvestIterator;
		private List<Planet> savedFromHarvest;
		private Planet savedPlanet;

		public RoundInformationHuds() {
			harvestIterator = findPlanetsUnderHarvest().iterator();
			savedFromHarvest = findPlanetsSavedFromHarvest();

			planetToMoves = new HashMap<String, List<Move>>();
			for (Move move : gameBoard.movesInProgress) {
				if (move.executed) {
					List<Move> planetMoves;
					if (planetToMoves.containsKey(move.to)) {
						planetMoves = planetToMoves.get(move.to);
					} else {
						planetMoves = new ArrayList<Move>();
					}
					planetMoves.add(move);
					planetToMoves.put(move.to, planetMoves);
				}
			}

			/*
			 * if an enemy transfer is detected without any attacks, don't show
			 * it
			 */
			List<String> keysToRemove = new ArrayList<String>();
			for (Map.Entry<String, List<Move>> planetMoves : planetToMoves.entrySet()) {
				boolean keepMove = false;
				for (int i = 0; i < planetMoves.getValue().size(); ++i) {
					Move move = planetMoves.getValue().get(i);
					Planet planet = gameBoard.getPlanet(move.to);
					if (!move.handle.equals(GameLoop.getUser().handle)
							&& move.battleStats.previousPlanetOwner.equals(planet.owner)
							&& !planet.owner.equals(GameLoop.getUser().handle)) {
						continue;
					}

					keepMove = true;
					break;
				}

				if (!keepMove) {
					keysToRemove.add(planetMoves.getKey());
				}
			}
			for (String key : keysToRemove) {
				planetToMoves.remove(key);
			}

			planetIterator = planetToMoves.keySet().iterator();
		}

		private List<Planet> findPlanetsUnderHarvest() {
			List<Planet> planetsUnderHarvest = new ArrayList<Planet>();
			for (Planet planet : gameBoard.planets) {
				if (planet.isUnderHarvest()) {
					planetsUnderHarvest.add(planet);
				}
			}
			return planetsUnderHarvest;
		}

		private List<Planet> findPlanetsSavedFromHarvest() {
			List<Planet> planetsSavedFromHarvest = new ArrayList<Planet>();
			for (Planet planet : gameBoard.planets) {
				if (planet.isSavedFromHarvest(gameBoard)) {
					planetsSavedFromHarvest.add(planet);
				}
			}
			return planetsSavedFromHarvest;
		}

		@Override
		public void createTopHud(List<Move> object) {
			Size size = screenCalcs.getTopHudBounds().size;
			topHud = new RoundInformationTopHud(gameBoard, resources, size.width, size.height);
		}

		@Override
		public void createBottomHud() {
			Size size = screenCalcs.getBottomHudBounds().size;
			bottomHud = new MultiPageBottomHud(resources, size.width, size.height);
			bottomHud.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					if (!(event instanceof NextPageEvent)) {
						return false;
					}

					HighlightOverlay.this.clear();
					bottomHud.changeButtonText("Next >");

					if (savedPlanet != null) {
						topHud.createPlanetSavedFromHarvestLabels(savedPlanet);
						HighlightOverlay.this.add(savedPlanet);
						savedPlanet = null;

						return true;
					}

					if (harvestIterator.hasNext()) {
						Planet planet = harvestIterator.next();
						topHud.createPlanetUnderHarvestLabels(planet);
						HighlightOverlay.this.add(planet);

						return true;
					}

					if (planetIterator.hasNext()) {
						List<Move> moves = planetToMoves.get(planetIterator.next());

						Planet toPlanet = gameBoard.getPlanet(moves.get(0).to);
						if (savedFromHarvest.contains(toPlanet)) {
							savedPlanet = toPlanet;
						}

						boolean airAttackOccured = false;
						for (Move move : moves) {
							if (move.battleStats.diedInAirAttack) {
								airAttackOccured = true;
								break;
							}
						}

						topHud.createAttackLabels(moves);

						if (airAttackOccured) {
							List<Move> player1Positions = new ArrayList<Move>();
							List<Move> player2Positions = new ArrayList<Move>();
							String player1 = null;
							for (final Move move : moves) {
								final Image moveToDisplay = MoveFactory.createShipForDisplay(
										move.angleOfMovement(gameBoard), move.previousPosition, resources, boardCalcs);

								Color color = Constants.Colors.USER_SHIP_FILL;
								if (!move.belongsToPlayer(GameLoop.getUser())) {
									color = Constants.Colors.ENEMY_SHIP_FILL;
								}
								moveToDisplay.setColor(color);
								addActor(moveToDisplay);

								if (player1 == null || player1.equals(move.handle)) {
									player1 = move.handle;
									player1Positions.add(move);
								} else {
									player2Positions.add(move);
								}

								if (move.battleStats.diedInAirAttack) {
									addExplosion(true, move.shipsToMove, move.previousPosition, 0.5f, color);
									moveToDisplay.addAction(delay(0.5f, alpha(0.0f, 0.25f)));
								} else {
									addAction(delay(1.0f, run(new Runnable() {
										@Override
										public void run() {
											moveToDisplay.remove();
											HighlightOverlay.this.add(move);
										}
									})));
								}
							}

							for (Move move1 : player1Positions) {
								for (Move move2 : player2Positions) {
									Point pixelPoint1 = boardCalcs.tileCoordsToPixels(move1.previousPosition);
									Point pixelPoint2 = boardCalcs.tileCoordsToPixels(move2.previousPosition);

									shootAtShip(pixelPoint1, pixelPoint2, move1);
									shootAtShip(pixelPoint2, pixelPoint1, move2);
								}
							}
						} else {
							for (Move move : moves) {
								HighlightOverlay.this.add(move);
							}
						}
					} else {
						hide();
						remove();
						onClose("");
					}

					return true;
				}
			});
		}

		private void shootAtShip(Point pixelPoint1, Point pixelPoint2, Move move) {
			float tileWidth = boardCalcs.getTileSize().width;
			float particleSize = tileWidth * 0.15f;

			Image particle = new Image(resources.skin, Constants.UI.EXPLOSION_PARTICLE);
			particle.setOrigin(particleSize * 0.5f, particleSize * 0.5f);
			particle.setBounds(pixelPoint1.x, pixelPoint1.y, particleSize, particleSize);
			particle.setColor(gameBoard.getPlanet(move.from).getColor(move.handle, false));

			boardCalcs.centerPoint(pixelPoint2, particle);
			particle.addAction(sequence(moveTo(pixelPoint2.x, pixelPoint2.y, 0.5f), alpha(0.0f)));

			addActor(particle);
		}

		@Override
		public void show() {
			Point topOrigin = screenCalcs.getTopHudBounds().origin;
			Point bottomOrigin = screenCalcs.getBottomHudBounds().origin;

			topHud.setX(topOrigin.x);
			topHud.setY(topOrigin.y);
			bottomHud.setX(bottomOrigin.x);
			bottomHud.setY(bottomOrigin.y);

			getStage().addActor(bottomHud);
			getStage().addActor(topHud);
		}

		@Override
		public void hide() {
			HighlightOverlay.this.hide(topHud, bottomHud);
		}
	}

	private class PlanetHuds implements Huds<Planet> {

		private PlanetInfoHud topHud;
		private Planet planet;

		@Override
		public void createTopHud(Planet planet) {
			topHud = new PlanetInfoHud(planet, gameBoard, resources, screenCalcs.getTopHudBounds().size.width,
					screenCalcs.getTopHudBounds().size.height);
			topHud.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					if (event instanceof HarvestEvent) {
						hide();
						remove();
						onClose("");
					}

					return false;
				}
			});

			this.planet = planet;
		}

		@Override
		public void createBottomHud() {

		}

		@Override
		public void show() {
			topHud.setPosition(0, Gdx.graphics.getHeight());
			getStage().addActor(topHud);

			topHud.addAction(moveTo(screenCalcs.getTopHudBounds().origin.x, screenCalcs.getTopHudBounds().origin.y,
					0.5f, Interpolation.circleOut));

			moveHud.saveMoves();
			moveHud.removeMoves();

			for (Move move : gameBoard.movesInProgress) {
				if (move.belongsToPlayer(GameLoop.getUser()) && move.to.equals(planet.name) && !move.executed) {
					Planet fromPlanet = gameBoard.getPlanet(move.from);
					PlanetButton fromPlanetButton = PlanetButtonFactory.createPlanetButton(fromPlanet, gameBoard, true,
							boardCalcs, resources);
					addActor(fromPlanetButton);
					highlightPath(fromPlanet, planet);

					moveHud.addMove(move);
				}
			}
		}

		public void hide() {
			moveHud.restoreMoves();

			HighlightOverlay.this.hide(topHud, null);
		}
	}

	private class MoveHuds implements Huds<Move> {

		private SingleMoveInfoHud moveInfoHud;
		private ShipSelectionHud shipSelectionHud;
		private ShaderLabel moveShipCount;
		private Move move;

		@Override
		public void createTopHud(Move move) {
			this.move = move;

			if (moveInfoHud == null) {
				moveInfoHud = new SingleMoveInfoHud(resources, screenCalcs.getTopHudBounds().size.width,
						screenCalcs.getTopHudBounds().size.height);
			}

			moveInfoHud.updateDuration(move.durationWithAbilityApplied(gameBoard));
			moveInfoHud.updateShips(move.shipsToMove);
		}

		private void updateShipCount(int value) {
			moveInfoHud.updateShips(value);
			if (moveShipCount == null) {
				moveShipCount = new ShaderLabel(resources.fontShader, "0", resources.skin, Constants.UI.X_LARGE_FONT,
						Color.WHITE);
				moveShipCount.setWidth(Gdx.graphics.getWidth());
				moveShipCount.setX(0);
				moveShipCount.setY(Gdx.graphics.getHeight() * 0.5f - moveShipCount.getHeight() * 0.5f);
				moveShipCount.setAlignment(Align.center, Align.center);
				moveShipCount.setTouchable(Touchable.disabled);
				addActor(moveShipCount);
			}
			moveShipCount.clearActions();
			moveShipCount.setText("" + value);
			moveShipCount.setColor(Color.WHITE);
			moveShipCount.addAction(alpha(0.0f, 0.8f));
		}

		@Override
		public void createBottomHud() {
			if (move.startingRound != gameBoard.roundInformation.round || GameLoop.getUser().hasMoved(gameBoard)) {
				return;
			}

			shipSelectionHud = new ShipSelectionHud(move, gameBoard.getPlanet(move.from).ships, resources);
			shipSelectionHud.addListener(new MoveListener() {

				@Override
				protected void performMove(int oldShipsToSend, Move move) {
					onCreateMove(oldShipsToSend, move);

					hide();
					remove();
				}

				@Override
				public void cancelDialog() {
					onCancel();

					hide();
					remove();
				}

				@Override
				public void sliderUpdate(int value) {
					updateShipCount(value);
				}
			});
		}

		@Override
		public void show() {
			moveInfoHud.setPosition(0, Gdx.graphics.getHeight());
			getStage().addActor(moveInfoHud);

			moveInfoHud.addAction(moveTo(screenCalcs.getTopHudBounds().origin.x,
					screenCalcs.getTopHudBounds().origin.y, 0.5f, Interpolation.circleOut));

			if (shipSelectionHud != null) {
				shipSelectionHud.setPosition(0, -screenCalcs.getBottomHudBounds().size.height);
				getStage().addActor(shipSelectionHud);

				shipSelectionHud.addAction(moveTo(screenCalcs.getBottomHudBounds().origin.x,
						screenCalcs.getBottomHudBounds().origin.y, 0.5f, Interpolation.circleOut));
			}
		}

		@Override
		public void hide() {
			if (moveShipCount != null) {
				moveShipCount.remove();
				moveShipCount = null;
			}

			HighlightOverlay.this.hide(moveInfoHud, shipSelectionHud);
		}
	}
}

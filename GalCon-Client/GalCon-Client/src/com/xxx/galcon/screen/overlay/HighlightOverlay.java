package com.xxx.galcon.screen.overlay;

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
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.math.GalConMath;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.model.RoundInformation;
import com.xxx.galcon.model.Size;
import com.xxx.galcon.model.factory.MoveFactory;
import com.xxx.galcon.model.factory.PlanetButtonFactory;
import com.xxx.galcon.screen.BoardScreen.BoardCalculations;
import com.xxx.galcon.screen.BoardScreen.ScreenCalculations;
import com.xxx.galcon.screen.MoveHud;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.event.MoveListener;
import com.xxx.galcon.screen.event.RoundInformationEvent;
import com.xxx.galcon.screen.hud.PlanetInfoHud;
import com.xxx.galcon.screen.hud.RoundInformationBottomHud;
import com.xxx.galcon.screen.hud.RoundInformationTopHud;
import com.xxx.galcon.screen.hud.ShipSelectionHud;
import com.xxx.galcon.screen.hud.SingleMoveInfoHud;
import com.xxx.galcon.screen.widget.PlanetButton;
import com.xxx.galcon.screen.widget.ShaderLabel;

public abstract class HighlightOverlay extends Overlay {
	private Huds huds;
	private ScreenCalculations screenCalcs;
	private BoardCalculations boardCalcs;
	private GameBoard gameBoard;
	private MoveHud moveHud;

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

		{
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, "Round", resources.skin, Constants.UI.LARGE_FONT);
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
			ShaderLabel lbl = new ShaderLabel(resources.fontShader, "" + (roundInformation.currentRound + 1),
					resources.skin, Constants.UI.LARGE_FONT);
			lbl.setWidth(Gdx.graphics.getWidth());
			lbl.setX(0);
			lbl.setY(Gdx.graphics.getHeight() * 0.6f - lbl.getHeight() * 0.5f);
			lbl.setAlignment(Align.center, Align.center);
			lbl.setTouchable(Touchable.disabled);
			lbl.setColor(Color.CLEAR);
			lbl.addAction(delay(0.33f, color(Color.WHITE, 0.66f)));
			addActor(lbl);
		}

		return this;
	}

	public void hide() {
		huds.hide();
		remove();
		onClose();
	}

	/**
	 * Called when the user dismisses the overlay
	 */
	public abstract void onClose();

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
		Planet fromPlanet = gameBoard.getPlanet(move.fromPlanet);
		Planet toPlanet = gameBoard.getPlanet(move.toPlanet);

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

		Image moveToDisplay = MoveFactory.createShipForDisplay(move.angleOfMovement(), position, resources, boardCalcs);

		Color color = Constants.Colors.USER_SHIP_FILL;
		if (!move.belongsToPlayer(GameLoop.USER)) {
			color = Constants.Colors.ENEMY_SHIP_FILL;
		}
		moveToDisplay.setColor(color);

		if (move.executed) {
			Point newShipPosition = MoveFactory.getShipPosition(moveToDisplay, move.currentPosition, boardCalcs);
			moveToDisplay.addAction(delay(0.0f, moveTo(newShipPosition.x, newShipPosition.y, 1.0f)));

			moveToDisplay.addAction(delay(0.25f, scaleTo(0, 0, 0.75f)));
			String previousOwner = toPlanetButton.planet.previousRoundOwner(gameBoard);
			toPlanetButton.showPlanetState(true, true);

			if (!previousOwner.equals(move.playerHandle)) {
				addExplosion(false, move.shipsToMove, move.endPosition, 1.0f, color);
			}
			
			if(!previousOwner.equals(move.playerHandle) && !toPlanetButton.planet.isOwnedBy(previousOwner)){
				addXpGainLabel(move.endPosition, toPlanetButton.planet.owner);
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
					moveBy(xStartOffset * 12, yStartOffset * 12, 1.5f, Interpolation.circleOut)));
			particle.addAction(sequence(delay(delay + 0.1f), rotateBy(1000, 2.0f)));
			particle.addAction(sequence(delay(delay + 1.0f), alpha(0.0f, 1.5f)));

			addActor(particle);
		}
	}
	
	private void addXpGainLabel(Point position, String planetOwner) {
		
		Point tileCenter = boardCalcs.tileCoordsToPixels(position);
		float yIncrease = boardCalcs.getTileSize().height * 0.65f;
				
		if(position.y >= gameBoard.heightInTiles - 1.5){
			yIncrease = yIncrease * -1f;
		}
		
		String fontColorToUse = Constants.UI.DEFAULT_FONT_RED;
		if(planetOwner.equals(GameLoop.USER.handle)){
			fontColorToUse = Constants.UI.DEFAULT_FONT_GREEN;
		}
		
		String labelText = gameBoard.gameConfig.getValue(Constants.XP_FROM_PLANET_CAPTURE);
		final ShaderLabel label = new ShaderLabel(resources.fontShader, "+"+ labelText, resources.skin, fontColorToUse);
		label.setX(tileCenter.x);
		label.setY(tileCenter.y);
		addActor(label);
		
		
		label.addAction(Actions.sequence(Actions.fadeOut(0.0f), Actions.delay(0.9f), Actions.fadeIn(0),  Actions.moveBy(0, yIncrease, 1.2f),
				Actions.fadeOut(0.6f), Actions.run(new Runnable() {
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
			particle.rotate((float) Math.toDegrees(angle));
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

	private class RoundInformationHuds implements Huds<List<Move>> {

		private RoundInformationBottomHud bottomHud;
		private RoundInformationTopHud topHud;

		private Map<String, List<Move>> planetToMoves;
		private Iterator<String> planetIterator;

		public RoundInformationHuds() {
			planetToMoves = new HashMap<String, List<Move>>();
			for (Move move : gameBoard.movesInProgress) {
				if (move.executed) {
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

			planetIterator = planetToMoves.keySet().iterator();
		}

		@Override
		public void createTopHud(List<Move> object) {
			Size size = screenCalcs.getTopHudBounds().size;
			topHud = new RoundInformationTopHud(gameBoard, resources, size.width, size.height);
		}

		@Override
		public void createBottomHud() {
			Size size = screenCalcs.getBottomHudBounds().size;
			bottomHud = new RoundInformationBottomHud(resources, size.width, size.height);
			bottomHud.addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					if (!(event instanceof RoundInformationEvent)) {
						return false;
					}

					if (planetIterator.hasNext()) {
						HighlightOverlay.this.clear();
						bottomHud.changeButtonText("Next >");
						List<Move> moves = planetToMoves.get(planetIterator.next());

						boolean airAttackOccured = false;
						for (Move move : moves) {
							if (move.battleStats.diedInAirAttack) {
								airAttackOccured = true;
								break;
							}
						}

						if (airAttackOccured) {
							List<Move> player1Positions = new ArrayList<Move>();
							List<Move> player2Positions = new ArrayList<Move>();
							String player1 = null;
							for (final Move move : moves) {
								final Image moveToDisplay = MoveFactory.createShipForDisplay(move.angleOfMovement(),
										move.previousPosition, resources, boardCalcs);

								Color color = Constants.Colors.USER_SHIP_FILL;
								if (!move.belongsToPlayer(GameLoop.USER)) {
									color = Constants.Colors.ENEMY_SHIP_FILL;
								}
								moveToDisplay.setColor(color);
								addActor(moveToDisplay);

								if (player1 == null || player1.equals(move.playerHandle)) {
									player1 = move.playerHandle;
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
							topHud.createAttackLabels(moves);
							for (Move move : moves) {
								HighlightOverlay.this.add(move);
							}
						}
					} else {
						hide();
						remove();
						onClose();
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
			particle.setColor(gameBoard.getPlanet(move.fromPlanet).getColor(move.playerHandle));

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
			topHud = new PlanetInfoHud(resources, screenCalcs.getTopHudBounds().size.width,
					screenCalcs.getTopHudBounds().size.height);
			topHud.updateRegen((int) planet.shipRegenRate);

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
				if (move.belongsToPlayer(GameLoop.USER) && move.toPlanet.equals(planet.name) && !move.executed) {
					Planet fromPlanet = gameBoard.getPlanet(move.fromPlanet);
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
			moveInfoHud.updateDuration(move.duration);
			moveInfoHud.updateShips(move.shipsToMove);
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
				addActor(moveShipCount);
			}
			moveShipCount.clearActions();
			moveShipCount.setText("" + value);
			moveShipCount.setColor(Color.WHITE);
			moveShipCount.addAction(alpha(0.0f, 0.8f));
		}

		@Override
		public void createBottomHud() {
			if (move.startingRound != gameBoard.roundInformation.currentRound || GameLoop.USER.hasMoved(gameBoard)) {
				return;
			}

			shipSelectionHud = new ShipSelectionHud(move, gameBoard.getPlanet(move.fromPlanet).numberOfShips, resources);
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

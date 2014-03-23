package com.xxx.galcon.screen.overlay;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
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

	public HighlightOverlay focus(RoundInformation roundInformation, List<Move> moves) {
		// we want users to use the 'go' button instead
		this.removeListener(defaultHideListener);

		huds = new RoundInformationHuds();
		huds.createTopHud(moves);
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

		final PlanetButton fromPlanetButton = PlanetButtonFactory.createPlanetButton(fromPlanet, gameBoard, true,
				boardCalcs, resources);
		final PlanetButton toPlanetButton = PlanetButtonFactory.createPlanetButton(toPlanet, gameBoard, true,
				boardCalcs, resources);

		final Image moveToDisplay = MoveFactory.createShipForDisplay(move.angleOfMovement(), move.currentPosition,
				resources, boardCalcs);

		addActor(fromPlanetButton);
		addActor(toPlanetButton);
		addActor(moveToDisplay);

		highlightPath(fromPlanet, toPlanet);

		return this;
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

		@Override
		public void createTopHud(List<Move> object) {
			Size size = screenCalcs.getTopHudBounds().size;
			topHud = new RoundInformationTopHud(resources, size.width, size.height);
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

					hide();
					remove();

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
				if (move.belongsToPlayer(GameLoop.USER) && move.toPlanet.equals(planet.name)) {
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

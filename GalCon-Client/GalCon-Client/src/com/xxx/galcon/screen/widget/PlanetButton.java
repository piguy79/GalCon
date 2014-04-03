package com.xxx.galcon.screen.widget;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.xxx.galcon.Constants;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.Resources;

public class PlanetButton extends Group {

	public Planet planet;
	private ShaderLabel label;
	private Image bg;
	private Image glowImage;
	private Resources resources;
	private GameBoard gameBoard;

	private float centerXUsed;
	private float centerYUsed;

	private Image ownedByHighlightImage;
	private Image ownedByImage;

	public PlanetButton(Resources resources, GameBoard gameBoard, boolean showCurrentState, Planet planet, float width,
			float height) {
		super();
		this.planet = planet;
		this.gameBoard = gameBoard;
		this.resources = resources;

		super.setWidth(width);
		super.setHeight(height);

		String planetTexture = "";
		if (!planet.isAlive()) {
			planetTexture = "dead_planet";
		} else {
			planetTexture = "planet-regen-" + (int) planet.regen;
		}

		bg = new Image(new TextureRegionDrawable(resources.planetAtlas.findRegion(planetTexture)));
		bg.setWidth(width);
		bg.setHeight(height);
		addActor(bg);

		showPlanetState(showCurrentState, false);

		label = new ShaderLabel(resources.fontShader, "0", resources.skin, Constants.UI.DEFAULT_FONT);
		label.setText("" + planet.numberOfShipsToDisplay(gameBoard, showCurrentState));
		positionLabel();
		addActor(label);
	}

	public void showPlanetState(boolean showCurrent, boolean animate) {
		Array<Action> highlightActions = new Array<Action>(Action.class);
		Array<Action> ownedActions = new Array<Action>(Action.class);

		String owner = planet.owner;
		String previousOwner = planet.previousRoundOwner(gameBoard);
		if (!showCurrent) {
			owner = previousOwner;
		}

		if (ownedByHighlightImage != null && showCurrent && !previousOwner.equals(planet.owner)
				&& !previousOwner.equals(Constants.OWNER_NO_ONE)) {
			float startDelay = 1.00f;
			highlightActions.add(delay(startDelay, alpha(0.0f, 0.5f)));
			ownedActions.add(delay(startDelay, color(Color.WHITE, 0.5f)));
		}

		if (ownedByHighlightImage == null && (planet.isHome || !owner.equals(Constants.OWNER_NO_ONE))) {
			ownedByHighlightImage = new Image(new TextureRegionDrawable(resources.planetAtlas.findRegion(planet
					.isAlive() ? "planet-highlight" : "dead_planet")));
			Color planetColor = planet.getColor(owner);
			planetColor.a = 0.0f;
			ownedByHighlightImage.setColor(planetColor);
			ownedByHighlightImage.setWidth(getWidth());
			ownedByHighlightImage.setHeight(getHeight());
			addActorAt(1, ownedByHighlightImage);

			ownedByImage = new Image(new TextureRegionDrawable(
					resources.planetAtlas.findRegion(planet.isAlive() ? "planet-owned" : "dead_planet")));
			ownedByImage.setWidth(getWidth());
			ownedByImage.setHeight(getHeight());
			ownedByImage.setColor(Color.CLEAR);
			addActorAt(2, ownedByImage);
		}

		if (ownedByHighlightImage != null) {
			float startDelay = highlightActions.size == 0 && animate ? 1.0f : 0.0f;
			float delay = animate ? 0.5f : 0.0f;
			Color planetColor = planet.getColor(owner);
			planetColor.a = 0.0f;
			highlightActions.add(sequence(delay(startDelay), parallel(color(planetColor, delay), alpha(0.6f, delay))));
			ownedActions.add(delay(startDelay, color(planet.getColor(owner), delay)));
		}

		if (highlightActions.size > 0) {
			ownedByHighlightImage.addAction(sequence(highlightActions.toArray()));
			ownedByImage.addAction(sequence(ownedActions.toArray()));
		}

		if (showCurrent && animate) {
			label.addAction(sequence(delay(1.0f), alpha(0.0f, 0.5f), run(new Runnable() {
				@Override
				public void run() {
					label.setText("" + planet.numberOfShipsToDisplay(gameBoard, true));
					positionLabel();
				}
			}), alpha(1.0f, 0.5f)));
		}
	}

	public void addGlow() {
		if (glowImage == null) {
			glowImage = new Image(new TextureRegionDrawable(resources.planetAtlas.findRegion("planet-glow")));
			glowImage.setWidth(getWidth());
			glowImage.setHeight(getHeight());

			glowImage.addAction(forever(sequence(color(Color.BLUE, 0.5f), color(Color.WHITE, 0.5f))));

			addActorAt(0, glowImage);
		}
	}

	public void removeGlow() {
		if (glowImage != null) {
			glowImage.remove();
			glowImage = null;
		}
	}

	private void positionLabel() {
		label.setX(getWidth() * 0.5f - (label.getTextBounds().width / 2));
		label.setY(getHeight() * 0.5f - (label.getTextBounds().height * 0.8f));
	}

	public Point centerPoint() {
		return new Point(getX() + getWidth() / 2, getY() + getHeight() / 2);
	}

	public void setShipCount(int shipCount) {
		planet.ships = shipCount;
		label.setText("" + shipCount);
		positionLabel();
	}

	public int getShipCount() {
		return planet.ships;
	}
}

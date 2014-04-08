package com.railwaygames.solarsmash.model.factory;

import java.util.List;

import com.railwaygames.solarsmash.math.GalConMath;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.Planet;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.BoardScreen.BoardCalculations;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.Moon;
import com.railwaygames.solarsmash.screen.widget.PlanetButton;

public class PlanetButtonFactory {

	private static List<Moon> allBoardScreenMoons;

	public PlanetButtonFactory() {
		super();
	}

	public static PlanetButton createPlanetButton(Planet planet, GameBoard gameBoard, boolean showCurrentState,
			BoardCalculations boardCalcs, Resources resources) {
		float maxExpand = 5;

		float expand = planet.regen > maxExpand ? maxExpand : planet.regen;
		float newPlanetSize = boardCalcs.getMinPlanetRadius() + ((boardCalcs.getMaxPlanetRadius() * 0.09f) * expand);

		PlanetButton button = new PlanetButton(resources, gameBoard, showCurrentState, planet, newPlanetSize,
				newPlanetSize);

		Point point = boardCalcs.tileCoordsToPixels(button.planet.position);
		boardCalcs.centerPoint(point, button);

		button.setX(point.x);
		button.setY(point.y);

		return button;
	}

	/**
	 * Slightly hackish. Assumes all game board moons are built first and passed
	 * in for later storage. Any later moon created in an overlay then links to
	 * the board screen moon. This is to keep moon positions in sync as they
	 * rotate.
	 */
	public static Moon createMoon(Resources resources, GameBoard gameBoard, PlanetButton planetButton,
			BoardCalculations boardCalcs, List<Moon> existingMoons) {
		if (existingMoons != null) {
			allBoardScreenMoons = existingMoons;
		}

		float width = boardCalcs.getTileSize().width * 0.4f;
		float height = boardCalcs.getTileSize().height * 0.4f;
		final Moon moon = new Moon(resources, gameBoard, planetButton, height, width);

		if (existingMoons == null) {
			for (Moon boardScreenMoon : allBoardScreenMoons) {
				if (boardScreenMoon.associatedPlanetButton.planet.name.equals(moon.associatedPlanetButton.planet.name)) {
					boardScreenMoon.setOverlayMoon(moon);
					break;
				}
			}
		}

		return moon;
	}

	public static Point findMoonPosition(Moon moon, BoardCalculations boardCalcs) {
		PlanetButton associatedPlanet = moon.associatedPlanetButton;
		Point movePoint = null;
		if (associatedPlanet != null) {
			if (moon.angle == 360) {
				moon.angle = 0;
			}

			movePoint = GalConMath.nextPointInEllipse(associatedPlanet.centerPoint(),
					boardCalcs.getTileSize().width * 0.7f, boardCalcs.getTileSize().height * 0.46f, moon.angle);
			moon.angle = (float) (moon.angle + moon.rateOfOrbit);
		}

		return movePoint;
	}
}

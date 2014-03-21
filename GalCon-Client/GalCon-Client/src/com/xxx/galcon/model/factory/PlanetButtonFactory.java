package com.xxx.galcon.model.factory;

import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.BoardScreen.BoardCalculations;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.widget.Moon;
import com.xxx.galcon.screen.widget.PlanetButton;

public class PlanetButtonFactory {

	public PlanetButtonFactory() {
		super();
	}

	public static PlanetButton createPlanetButton(Planet planet, GameBoard gameBoard, boolean roundAnimated,
			BoardCalculations boardCalcs, Resources resources) {
		float maxExpand = 5;

		float expand = planet.shipRegenRate > maxExpand ? maxExpand : planet.shipRegenRate;
		float newPlanetSize = boardCalcs.getMinPlanetRadius() + ((boardCalcs.getMaxPlanetRadius() * 0.09f) * expand);

		PlanetButton button = new PlanetButton(resources, "" + planet.numberOfShipsToDisplay(gameBoard, roundAnimated),
				planet, newPlanetSize, newPlanetSize);

		Point point = boardCalcs.tileCoordsToPixels(button.planet.position);
		boardCalcs.centerPoint(point, button);

		button.setX(point.x);
		button.setY(point.y);

		return button;
	}

	public static Moon createMoon(Resources resources, PlanetButton planetButton, BoardCalculations boardCalcs) {
		float width = boardCalcs.getTileSize().width * 0.4f;
		float height = boardCalcs.getTileSize().height * 0.4f;
		final Moon moon = new Moon(resources, planetButton, height, width);

		return moon;
	}

}

package com.railwaygames.solarsmash.model.factory;

import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.Planet;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.BoardScreen.BoardCalculations;
import com.railwaygames.solarsmash.screen.widget.Moon;
import com.railwaygames.solarsmash.screen.widget.PlanetButton;

public class PlanetButtonFactory {

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

	public static Moon createMoon(Resources resources, PlanetButton planetButton, BoardCalculations boardCalcs) {
		float width = boardCalcs.getTileSize().width * 0.4f;
		float height = boardCalcs.getTileSize().height * 0.4f;
		final Moon moon = new Moon(resources, planetButton, height, width);

		return moon;
	}

}

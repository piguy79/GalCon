package com.xxx.galcon.model.factory;

import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.screen.BoardScreen.BoardCalculations;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.widget.Moon;
import com.xxx.galcon.screen.widget.PlanetButton;

public class PlanetButtonFactory {

	public PlanetButtonFactory() {
		super();
	}

	public static PlanetButton createPlanetButtonWithExpansion(Planet planet, GameBoard gameBoard,
			boolean roundAnimated, BoardCalculations boardCalcs, Resources resources) {
		float maxExpand = 5;
		float expand = planet.shipRegenRate > maxExpand ? maxExpand : planet.shipRegenRate;
		float newPlanetSize = boardCalcs.getMinPlanetRadius() + ((boardCalcs.getMaxPlanetRadius() * 0.09f) * expand);

		return createPlanetButton(planet, gameBoard, roundAnimated, newPlanetSize, newPlanetSize, resources);
	}

	public static PlanetButton createPlanetButton(Planet planet, GameBoard gameBoard, boolean roundAnimated,
			float width, float height, Resources resources) {

		final PlanetButton planetButton = new PlanetButton(resources.assetManager, ""
				+ planet.numberOfShipsToDisplay(gameBoard, roundAnimated), planet, resources.fontShader,
				resources.skin, width, height);

		return planetButton;
	}

	public static Moon createMoon(Resources resources, Planet planet, BoardCalculations boardCalcs) {
		float width = boardCalcs.getTileSize().width * 0.4f;
		float height = boardCalcs.getTileSize().height * 0.4f;
		final Moon moon = new Moon(resources.assetManager, planet, height, width);
		moon.setHeight(height);
		moon.setWidth(width);

		return moon;
	}

}

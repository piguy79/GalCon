package com.xxx.galcon.model.factory;

import static com.xxx.galcon.Util.createShader;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.screen.widget.Moon;
import com.xxx.galcon.screen.widget.PlanetButton;

public class PlanetButtonFactory {

	private static ShaderProgram fontShader;

	private static float minPlanetSize;
	public static float tileWidthInWorld;
	public static float tileHeightInWorld;

	private static AssetManager assetManager;
	private static UISkin skin;

	public static void setup(AssetManager assetManager, float tileWidthInWorld, float tileHeightInWorld, UISkin skin) {
		PlanetButtonFactory.tileWidthInWorld = tileWidthInWorld;
		PlanetButtonFactory.tileHeightInWorld = tileHeightInWorld;
		PlanetButtonFactory.fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");

		float largest = Math.max(tileWidthInWorld, tileHeightInWorld);
		PlanetButtonFactory.minPlanetSize = largest * 0.5f;

		PlanetButtonFactory.assetManager = assetManager;
		PlanetButtonFactory.skin = skin;
	}

	public PlanetButtonFactory() {
		super();
	}

	public static PlanetButton createPlanetButtonWithExpansion(Planet planet, GameBoard gameBoard, boolean roundAnimated) {
		float maxExpand = 5;
		float expand = planet.shipRegenRate > maxExpand ? maxExpand : planet.shipRegenRate;
		float largest = Math.max(tileWidthInWorld, tileHeightInWorld);
		float newPlanetSize = minPlanetSize + ((largest * 0.09f) * expand);

		return createPlanetButton(planet, gameBoard, roundAnimated, newPlanetSize, newPlanetSize);
	}

	public static PlanetButton createPlanetButton(Planet planet, GameBoard gameBoard, boolean roundAnimated,
			float width, float height) {

		final PlanetButton planetButton = new PlanetButton(assetManager, ""
				+ planet.numberOfShipsToDisplay(gameBoard, roundAnimated), planet, fontShader, skin, width, height);

		return planetButton;
	}

	public static Moon createMoon(AssetManager assetManager, Planet planet, float height, float width) {
		final Moon moon = new Moon(assetManager, planet, height, width);
		moon.setHeight(height);
		moon.setWidth(width);

		return moon;
	}

}

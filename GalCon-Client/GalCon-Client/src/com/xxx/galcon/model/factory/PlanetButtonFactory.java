package com.xxx.galcon.model.factory;

import static com.xxx.galcon.Util.createShader;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.screen.widget.PlanetButton;

public class PlanetButtonFactory {
	
	private static TextureRegionDrawable planetTexture;
	private static TextButtonStyle style;
	private static TextureAtlas planetAtlas;
	private static ShaderProgram fontShader;
	
	private static float minPlanetSize;
	public static float tileWidthInWorld;
	public static float tileHeightInWorld;
	
	public static void setup(AssetManager assetManager, float tileWidthInWorld, float tileHeightInWorld){
		PlanetButtonFactory.tileWidthInWorld = tileWidthInWorld;
		PlanetButtonFactory.tileHeightInWorld = tileHeightInWorld;
		PlanetButtonFactory.fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");

		PlanetButtonFactory.planetAtlas = assetManager.get("data/images/planets.atlas", TextureAtlas.class);
		PlanetButtonFactory.planetTexture = new TextureRegionDrawable(planetAtlas.findRegion("base_planet"));
		PlanetButtonFactory.style = new TextButtonStyle(planetTexture, planetTexture, planetTexture, Fonts.getInstance(
				assetManager).mediumFont());
		
		PlanetButtonFactory.minPlanetSize = tileWidthInWorld * 0.4f;
		PlanetButtonFactory.planetTexture.setMinWidth(minPlanetSize);
		PlanetButtonFactory.planetTexture.setMinHeight(minPlanetSize);
	}
	
	
	public PlanetButtonFactory() {
		super();
	}


	public static PlanetButton createPlanetButtonWithExpansion(Planet planet, GameBoard gameBoard, boolean roundAnimated){
		float maxExpand = 5;
		float expand = planet.shipRegenRate > maxExpand ? maxExpand : planet.shipRegenRate;
		float newPlanetSize = minPlanetSize + ((tileWidthInWorld * 0.08f) * expand);
		
		
		return createPlanetButton(planet, gameBoard, roundAnimated, newPlanetSize, newPlanetSize);
	}
	
	public static PlanetButton createPlanetButton(Planet planet, GameBoard gameBoard, boolean roundAnimated, float width, float height){
		planetTexture.setMinWidth(width);
		planetTexture.setMinHeight(height);
		final PlanetButton planetButton = new PlanetButton(fontShader, ""
				+ planet.numberOfShipsToDisplay(gameBoard, roundAnimated), style, planet);
		planetButton.setColor(planet.getColor());
		planetButton.setHeight(height);
		planetButton.setWidth(width);
		
		return planetButton;
		
		
	}

}

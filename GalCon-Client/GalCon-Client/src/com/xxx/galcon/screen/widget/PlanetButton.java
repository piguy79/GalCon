package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;

public class PlanetButton extends Group {
	
	public Planet planet;
	private TextureAtlas planetAtlas;
	private ShaderLabel label;
	private Image bg;
	
	private float centerXUsed;
	private float centerYUsed;
	
	public PlanetButton(AssetManager assetManager, String countToDisplay, Planet planet, ShaderProgram fontShader, UISkin skin, float width, float height) {
		super();
		this.planet = planet;
		this.planetAtlas = assetManager.get("data/images/planets.atlas", TextureAtlas.class);
		super.setWidth(width);
		super.setHeight(height);
		
		bg = new Image(new TextureRegionDrawable(planetAtlas.findRegion(planet.isAlive() ? "planet4" :"dead_planet")));
		bg.setWidth(width);
		bg.setHeight(height);
		bg.setColor(planet.getColor());
		label = new ShaderLabel(fontShader, countToDisplay, skin, Constants.UI.DEFAULT_FONT);
		positionLabel();
		
		addActor(bg);
		addActor(label);
	}
	
	private void positionLabel(){
		Point center =  centerPoint();
		centerXUsed = center.x;
		centerYUsed = center.y;
		positionWithCachedValues();
	}

	private void positionWithCachedValues() {
		label.setX(centerXUsed - (label.getTextBounds().width / 2));
		label.setY(centerYUsed - (label.getTextBounds().height * 0.6f));
	}

	public Point centerPoint() {
		return new Point(getX() + (getWidth() / 2), getY() + (getHeight() / 2));
	}
	
	public void setText(String text){
		label.setText(text);
		positionWithCachedValues();
	}
	

}

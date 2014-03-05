package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.Resources;

public class PlanetButton extends Group {

	public Planet planet;
	private ShaderLabel label;
	private Image bg;

	private float centerXUsed;
	private float centerYUsed;

	public PlanetButton(Resources resources, String countToDisplay, Planet planet, float width, float height) {
		super();
		this.planet = planet;
		super.setWidth(width);
		super.setHeight(height);

		bg = new Image(new TextureRegionDrawable(resources.planetAtlas.findRegion(planet.isAlive() ? "planet4"
				: "dead_planet")));
		bg.setWidth(width);
		bg.setHeight(height);
		bg.setColor(planet.getColor());
		label = new ShaderLabel(resources.fontShader, countToDisplay, resources.skin, Constants.UI.DEFAULT_FONT);
		positionLabel();

		addActor(bg);
		addActor(label);
	}

	private void positionLabel() {
		Point center = centerPoint();
		centerXUsed = center.x;
		centerYUsed = center.y;
		positionWithCachedValues();
	}

	private void positionWithCachedValues() {
		label.setX(centerXUsed - (label.getTextBounds().width / 2));
		label.setY(centerYUsed - (label.getTextBounds().height * 0.8f));
	}

	public Point centerPoint() {
		return new Point(getX() + (getWidth() / 2), getY() + (getHeight() / 2));
	}

	public void setShipCount(int shipCount) {
		planet.numberOfShips = shipCount;
		label.setText("" + shipCount);
		positionWithCachedValues();
	}

	public int getShipCount() {
		return planet.numberOfShips;
	}
}

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

		String planetTexture = "planet-regen-3";
		if (!planet.isAlive()) {
			planetTexture = "dead_planet";
		} else {
			planetTexture = "planet-regen-" + (int) planet.shipRegenRate;
		}

		bg = new Image(new TextureRegionDrawable(resources.planetAtlas.findRegion(planetTexture)));
		bg.setWidth(width);
		bg.setHeight(height);
		addActor(bg);

		if (planet.isOwned()) {
			Image ownedByHighlightImage = new Image(new TextureRegionDrawable(resources.planetAtlas.findRegion(planet
					.isAlive() ? "planet-highlight" : "dead_planet")));
			Color planetColor = planet.getColor();
			planetColor.a = 0.6f;
			ownedByHighlightImage.setColor(planetColor);
			ownedByHighlightImage.setWidth(width);
			ownedByHighlightImage.setHeight(height);
			addActor(ownedByHighlightImage);

			Image ownedByImage = new Image(new TextureRegionDrawable(
					resources.planetAtlas.findRegion(planet.isAlive() ? "planet-owned" : "dead_planet")));
			ownedByImage.setColor(planet.getColor());
			ownedByImage.setWidth(width);
			ownedByImage.setHeight(height);
			addActor(ownedByImage);
		}

		label = new ShaderLabel(resources.fontShader, countToDisplay, resources.skin, Constants.UI.DEFAULT_FONT);
		positionLabel();
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

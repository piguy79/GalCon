package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;

public class PlanetButton extends ShaderTextButton {
	
	public Planet planet;
	
	public PlanetButton(ShaderProgram shader, String text, TextButtonStyle style, Planet planet) {
		super(shader, text, style);
		this.planet = planet;
	}

	public Point centerPoint() {
		return new Point(getX() + (getWidth() / 2), getY() + (getHeight() / 2));
	}

}

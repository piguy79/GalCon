package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.xxx.galcon.model.Planet;

public class PlanetButton extends ShaderTextButton {
	
	public Planet planet;

	public PlanetButton(ShaderProgram shader, String text, TextButtonStyle style, Planet planet) {
		super(shader, text, style);
		this.planet = planet;
	}

}

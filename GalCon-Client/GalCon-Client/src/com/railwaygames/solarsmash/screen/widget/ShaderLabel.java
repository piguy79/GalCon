package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ShaderLabel extends Label {

	private ShaderProgram shader;

	public ShaderLabel(ShaderProgram shader, CharSequence text, Skin skin, String styleName, Color color) {
		super(text, skin, styleName, color);
		this.shader = shader;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		batch.setShader(shader);
		super.draw(batch, parentAlpha);
		batch.setShader(null);
	}
}

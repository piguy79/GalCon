package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ShaderLabel extends Label {

	private ShaderProgram shader;

	public ShaderLabel(ShaderProgram shader, CharSequence text, Skin skin, String styleName) {
		super(text, skin, styleName);
		this.shader = shader;
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		batch.setShader(shader);
		super.draw(batch, parentAlpha);
		batch.setShader(null);
	}
}

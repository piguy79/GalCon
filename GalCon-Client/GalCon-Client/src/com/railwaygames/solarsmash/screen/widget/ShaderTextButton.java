package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class ShaderTextButton extends TextButton {

	private ShaderProgram shader;

	public ShaderTextButton(ShaderProgram shader, String text, Skin skin, String style) {
		super(text, skin, style);
		this.shader = shader;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		batch.setShader(shader);
		super.draw(batch, parentAlpha);
		batch.setShader(null);
	}
}

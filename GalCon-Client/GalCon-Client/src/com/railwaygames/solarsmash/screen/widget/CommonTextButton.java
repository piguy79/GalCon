package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.UISkin;

public class CommonTextButton extends Group {

	private ImageButton bg;
	private ShaderLabel label;
	private String text;
	private float height;
	private float width;
	private UISkin skin;
	private ShaderProgram fontShader;

	public CommonTextButton(UISkin skin, String text, float height, float width, ShaderProgram fontShader) {
		super();
		this.skin = skin;
		this.text = text;
		this.height = height;
		this.width = width;
		this.fontShader = fontShader;

		create();
	}

	private void create() {
		setHeight(height);
		setWidth(width);

		label = new ShaderLabel(fontShader, text, skin, Constants.UI.DEFAULT_FONT, Color.BLACK);
		label.setAlignment(Align.center);
		label.setWrap(true);
		label.setX(0);
		label.setY((getHeight() / 2) - (label.getTextBounds().height / 1.25f));
		label.setWidth(getWidth());

		bg = new ImageButton(skin, Constants.UI.BASIC_BUTTON);
		bg.setLayoutEnabled(false);
		bg.setX(0);
		bg.setY(0);
		bg.setWidth(getWidth());
		bg.setHeight(getHeight());

		addActor(bg);
		addActor(label);

	}

}

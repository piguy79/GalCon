package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.UISkin;

public class CommonCoinButton extends Group {

	private Button bg;
	private Image coinImage;
	private ShaderLabel label;
	private String text;
	private float height;
	private float width;
	private UISkin skin;
	private ShaderProgram fontShader;

	public CommonCoinButton(UISkin skin, String text, float height, float width, ShaderProgram fontShader) {
		super();
		this.skin = skin;
		this.text = text;
		this.height = height;
		this.width = width;
		this.fontShader = fontShader;

		create();
	}

	public Image getCoinImage() {
		return coinImage;
	}

	public void create() {
		setHeight(height);
		setWidth(width);

		label = new ShaderLabel(fontShader, text, skin, Constants.UI.DEFAULT_FONT, Color.BLACK);
		label.setAlignment(Align.center);
		label.setWrap(true);
		label.setX(0);
		label.setY((getHeight() / 2) - (label.getTextBounds().height / 1.25f));
		label.setWidth(getWidth() * 0.75f);

		bg = new Button(skin, Constants.UI.BASIC_BUTTON);
		bg.setX(0);
		bg.setY(0);
		bg.setWidth(getWidth());
		bg.setHeight(getHeight());

		coinImage = new Image(skin, Constants.UI.COIN_IMAGE);
		coinImage.setX(getWidth() * 0.8f);
		coinImage.setWidth(getWidth() * 0.18f);
		coinImage.setHeight(coinImage.getWidth());
		coinImage.setY((getHeight() * 0.5f) - (coinImage.getHeight() * 0.5f));

		addActor(bg);
		addActor(label);
		addActor(coinImage);
	}
}

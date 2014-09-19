package com.railwaygames.solarsmash.screen.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.UISkin;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.event.NextPageEvent;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class MultiPageBottomHud extends Group {
	private Resources resources;
	private Button nextButton;
	private ShaderLabel nextText;

	private AtlasRegion bgRegion;

	public MultiPageBottomHud(Resources resources, float width, float height) {
		this.resources = resources;

		setHeight(height);
		setWidth(width);

		createBackground();
		createNextButton(resources.fontShader, resources.skin);
	}

	private void createNextButton(ShaderProgram fontShader, UISkin skin) {
		nextButton = new Button(skin, Constants.UI.BASIC_BUTTON);
		float bWidth = getWidth() * 0.3f;
		float bHeight = bWidth * 0.4f;
		nextButton.setBounds(getWidth() * 0.5f - bWidth * 0.5f, getHeight() * 0.5f - bHeight * 0.5f, bWidth, bHeight);

		nextText = new ShaderLabel(fontShader, "Go", skin, Constants.UI.DEFAULT_FONT, Color.BLACK);
		nextText.setAlignment(Align.center);
		nextText.setY(nextButton.getY() + nextButton.getHeight() / 2 - nextText.getHeight() * 0.5f);
		nextText.setWidth(getWidth());

		addActor(nextButton);
		addActor(nextText);

		nextButton.addListener(nextListener);
		nextText.addListener(nextListener);
	}

	private NextListener nextListener = new NextListener();

	private class NextListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			fire(new NextPageEvent());
		}
	}

	private void createBackground() {
		bgRegion = resources.gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}

	public void changeButtonText(String text) {
		nextText.setText(text);
	}
}

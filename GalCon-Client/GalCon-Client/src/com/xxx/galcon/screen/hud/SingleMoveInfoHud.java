package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class SingleMoveInfoHud extends Group {
	private AssetManager assetManager;
	private ShaderProgram fontShader;
	private UISkin skin;
	private ShaderLabel shipsLabel;
	private ShaderLabel durationLabel;

	private AtlasRegion bgRegion;

	public SingleMoveInfoHud(AssetManager assetManager, ShaderProgram fontShader, UISkin skin, float width, float height) {
		this.assetManager = assetManager;
		this.fontShader = fontShader;
		this.skin = skin;

		setHeight(height);
		setWidth(width);

		createBackground();
		createLabels();
	}

	private void createLabels() {
		{
			ShaderLabel label = new ShaderLabel(fontShader, "Sending", skin, Constants.UI.DEFAULT_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getX());
			label.setY(getHeight() * 0.7f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.5f);
			label.setAlignment(Align.center, Align.center);
			addActor(label);
		}
		{
			ShaderLabel label = new ShaderLabel(fontShader, "0", skin, Constants.UI.LARGE_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getX());
			label.setY(getHeight() * 0.15f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.5f);
			label.setAlignment(Align.center, Align.center);
			addActor(label);

			shipsLabel = label;
		}
		{
			ShaderLabel label = new ShaderLabel(fontShader, "Turns to arrive", skin, Constants.UI.DEFAULT_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getWidth() * 0.5f);
			label.setY(getHeight() * 0.7f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.5f);
			label.setAlignment(Align.center, Align.center);
			addActor(label);
		}
		{
			ShaderLabel label = new ShaderLabel(fontShader, "0", skin, Constants.UI.LARGE_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getWidth() * 0.5f);
			label.setY(getHeight() * 0.15f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.5f);
			label.setAlignment(Align.center, Align.center);
			addActor(label);

			durationLabel = label;
		}
	}

	private void createBackground() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		bgRegion = gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}

	public void updateShips(int shipsToMove) {
		shipsLabel.setText("" + shipsToMove);
	}

	public void updateDuration(int duration) {
		durationLabel.setText("" + duration);
	}

	public void updateDuration(float duration) {
		durationLabel.setText("" + (int) (Math.ceil(duration)));
	}
}

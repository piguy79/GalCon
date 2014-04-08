package com.railwaygames.solarsmash.screen.hud;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class SingleMoveInfoHud extends Group {
	private Resources resources;
	private ShaderLabel shipsLabel;
	private ShaderLabel durationLabel;

	private AtlasRegion bgRegion;

	public SingleMoveInfoHud(Resources resources, float width, float height) {
		this.resources = resources;

		setHeight(height);
		setWidth(width);

		createBackground();
		createLabels();
	}

	private void createLabels() {
		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "# of ships", resources.skin,
					Constants.UI.DEFAULT_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getX());
			label.setY(getHeight() * 0.7f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.4f);
			label.setAlignment(Align.center, Align.center);
			addActor(label);
		}
		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "0", resources.skin, Constants.UI.LARGE_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getX());
			label.setY(getHeight() * 0.15f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.4f);
			label.setAlignment(Align.center, Align.center);
			addActor(label);

			shipsLabel = label;
		}
		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "Rounds to impact", resources.skin,
					Constants.UI.DEFAULT_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getWidth() * 0.4f);
			label.setY(getHeight() * 0.7f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.6f);
			label.setAlignment(Align.center, Align.center);
			addActor(label);
		}
		{
			ShaderLabel label = new ShaderLabel(resources.fontShader, "0", resources.skin, Constants.UI.LARGE_FONT);
			TextBounds bounds = label.getTextBounds();
			label.setX(getWidth() * 0.4f);
			label.setY(getHeight() * 0.15f - bounds.height * 0.5f);
			label.setWidth(getWidth() * 0.6f);
			label.setAlignment(Align.center, Align.center);
			addActor(label);

			durationLabel = label;
		}
	}

	private void createBackground() {
		bgRegion = resources.gameBoardAtlas.findRegion("player_hud");
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
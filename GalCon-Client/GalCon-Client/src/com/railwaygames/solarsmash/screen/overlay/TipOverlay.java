package com.railwaygames.solarsmash.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.widget.CommonTextButton;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class TipOverlay extends Overlay {
	
	private ShaderLabel tipLabel;
	private String tipText;
	private CommonTextButton okButton;
	
	private AtlasRegion bgRegion;

	public TipOverlay(String tip, Resources resources) {
		super(resources);
		this.tipText = tip;
		createBackground();
		createTip();
		createOkButton();
	}
	
	private void createBackground() {
		bgRegion = resources.gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(Gdx.graphics.getWidth());
		backGround.setHeight(Gdx.graphics.getHeight() * 0.1f);
		addActor(backGround);
	}

	private void createOkButton() {
		float bWidth = Gdx.graphics.getWidth() * 0.3f;
		okButton = new CommonTextButton(resources.skin, "OK", bWidth * 0.4f, bWidth , resources.fontShader);
		okButton.setX((Gdx.graphics.getWidth() / 2) - (okButton.getWidth() / 2));
		okButton.setY((Gdx.graphics.getHeight() * 0.05f) - (okButton.getHeight() / 2));
		
		addActor(okButton);
	}

	private void createTip() {
		tipLabel = new ShaderLabel(resources.fontShader, tipText, resources.skin, Constants.UI.DEFAULT_FONT, Color.WHITE);
		tipLabel.setBounds(0, (Gdx.graphics.getHeight() * 0.5f) - (tipLabel.getTextBounds().height / 2), Gdx.graphics.getWidth(), tipLabel.getTextBounds().height);
		tipLabel.setWrap(true);
		tipLabel.setAlignment(Align.center);
		
		addActor(tipLabel);
	}

}

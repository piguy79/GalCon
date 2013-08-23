package com.xxx.galcon;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class UISkin extends Skin {

	public void initialize(AssetManager assetManager) {
		/*
		 * Labels
		 */
		add("default", new LabelStyle(Fonts.getInstance().largeFont(), Color.RED));

		/*
		 * Image Buttons
		 */
		Texture regularPlay = assetManager.get("data/images/reg_play.png", Texture.class);
		TextureRegionDrawable textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(regularPlay));
		add("regularPlay", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		Texture socialPlay = assetManager.get("data/images/social_play.png", Texture.class);
		textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(socialPlay));
		add("socialPlay", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		Texture backTexture = assetManager.get("data/images/back.png", Texture.class);
		textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(backTexture));
		add("backButton", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		Texture googlePlusTex = assetManager.get("data/images/Google+_chiclet_Red.jpg", Texture.class);
		textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(googlePlusTex));
		add("googlePlusButton", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));
	}
}

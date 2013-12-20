package com.xxx.galcon;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.screen.widget.ShaderTextField.ShaderTextFieldStyle;

public class UISkin extends Skin {

	public void initialize(AssetManager assetManager) {

		TextureAtlas socialAtlas = assetManager.get("data/images/social.atlas", TextureAtlas.class);
		TextureAtlas levelSelectionAtlas = assetManager.get("data/images/levelSelection.atlas", TextureAtlas.class);
		TextureAtlas menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);

		/*
		 * Labels
		 */
		add("default", new LabelStyle(Fonts.getInstance(assetManager).largeFont(), Color.RED));
		add(Constants.UI.LARGE_FONT, new LabelStyle(Fonts.getInstance(assetManager).largeFont(), Color.WHITE));
		add(Constants.UI.DEFAULT_FONT, new LabelStyle(Fonts.getInstance(assetManager).mediumFont(), Color.WHITE));
		add(Constants.UI.DEFAULT_FONT_BLACK, new LabelStyle(Fonts.getInstance(assetManager).mediumFont(), Color.BLACK));
		add(Constants.UI.SMALL_FONT, new LabelStyle(Fonts.getInstance(assetManager).smallFont(), Color.WHITE));
		
		/*
		 * TextField
		 */
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("textFieldBg"));
			trd.setLeftWidth(20);
			trd.setRightWidth(20);
			TextureRegionDrawable cursor = new TextureRegionDrawable(menusAtlas.findRegion("cursor"));
			add(Constants.UI.TEXT_FIELD, new ShaderTextFieldStyle(Fonts.getInstance(assetManager).mediumFont(),
					Color.BLACK, cursor, null, trd));
		}
		/*
		 * Colors
		 */
		add(Constants.UI.DEFAULT_BG_COLOR, new Color(0.0f, 0.0f, 0.0f, 1.0f), Color.class);

		/*
		 * ImageText Buttons
		 */
		{
			NinePatch up = socialAtlas.createPatch("common_signin_btn_icon_normal_dark");
			NinePatch down = socialAtlas.createPatch("common_signin_btn_icon_pressed_dark");

			ButtonStyle style = new ButtonStyle(new NinePatchDrawable(up), new NinePatchDrawable(down), null);
			add(Constants.UI.GOOGLE_PLUS_SIGN_IN_BUTTON, style);
		}
		
		/*
		 * Image
		 */
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("dialog_bg"));
			add(Constants.UI.CELL_BG, trd, Drawable.class);
		}

		/*
		 * Image Buttons
		 */
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("wait"));
			add(Constants.UI.WAIT_BUTTON, new ImageButtonStyle(trd, trd, trd, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("ok_button"));
			add(Constants.UI.OK_BUTTON, new ImageButtonStyle(trd, trd, trd, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("dialog_bg"));
			add(Constants.UI.DIALOG_BG, new ImageButtonStyle(trd, trd, trd, trd, trd, trd));
		}

		TextureRegionDrawable textureRegionDrawable = new TextureRegionDrawable(
				levelSelectionAtlas.findRegion("reg_play"));
		add("regularPlay", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		textureRegionDrawable = new TextureRegionDrawable(levelSelectionAtlas.findRegion("social_play"));
		add("socialPlay", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		textureRegionDrawable = new TextureRegionDrawable(menusAtlas.findRegion("back"));
		add("backButton", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		textureRegionDrawable = new TextureRegionDrawable(menusAtlas.findRegion("cancel_button"));
		add("cancelButton", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		textureRegionDrawable = new TextureRegionDrawable(menusAtlas.findRegion("ok_button"));
		add("okButton", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		textureRegionDrawable = new TextureRegionDrawable(socialAtlas.findRegion("Google+_chiclet_Red"));
		add("googlePlusButton", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		textureRegionDrawable = new TextureRegionDrawable(menusAtlas.findRegion("end_turn"));
		add("performMoveButton", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

		textureRegionDrawable = new TextureRegionDrawable(gameBoardAtlas.findRegion("ship"));
		add("shipButton", new ImageButtonStyle(textureRegionDrawable, textureRegionDrawable, textureRegionDrawable,
				textureRegionDrawable, textureRegionDrawable, textureRegionDrawable));

	}
}

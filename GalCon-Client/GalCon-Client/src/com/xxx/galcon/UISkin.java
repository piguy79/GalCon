package com.xxx.galcon;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
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
		add(Constants.UI.X_LARGE_FONT, new LabelStyle(Fonts.getInstance(assetManager).xLargeFont(), Color.WHITE));
		add(Constants.UI.LARGE_FONT, new LabelStyle(Fonts.getInstance(assetManager).largeFont(), Color.WHITE));
		add(Constants.UI.LARGE_FONT_BLACK, new LabelStyle(Fonts.getInstance(assetManager).largeFont(), Color.BLACK));
		add(Constants.UI.DEFAULT_FONT, new LabelStyle(Fonts.getInstance(assetManager).mediumFont(), Color.WHITE));
		add(Constants.UI.DEFAULT_FONT_BLACK, new LabelStyle(Fonts.getInstance(assetManager).mediumFont(), Color.BLACK));
		add(Constants.UI.DEFAULT_FONT_GREEN, new LabelStyle(Fonts.getInstance(assetManager).mediumFont(), Color.GREEN));
		add(Constants.UI.DEFAULT_FONT_RED, new LabelStyle(Fonts.getInstance(assetManager).mediumFont(), Color.RED));
		add(Constants.UI.DEFAULT_FONT_YELLOW,
				new LabelStyle(Fonts.getInstance(assetManager).mediumFont(), Color.YELLOW));
		add(Constants.UI.X_SMALL_FONT, new LabelStyle(Fonts.getInstance(assetManager).xSmallFont(), Color.WHITE));
		add(Constants.UI.SMALL_FONT, new LabelStyle(Fonts.getInstance(assetManager).smallFont(), Color.WHITE));
		add(Constants.UI.X_SMALL_FONT_GREEN, new LabelStyle(Fonts.getInstance(assetManager).xSmallFont(), new Color(
				0.1f, 1.0f, 0.1f, 1)));
		add(Constants.UI.X_SMALL_FONT_RED, new LabelStyle(Fonts.getInstance(assetManager).xSmallFont(), new Color(1.0f,
				0.3f, 0.3f, 1)));
		add(Constants.UI.BASIC_BUTTON_TEXT, new LabelStyle(Fonts.getInstance(assetManager).mediumFont(), Color.BLACK));

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
		 * TextButton
		 */
		{
			TextButtonStyle tbs = new TextButtonStyle();
			tbs.font = Fonts.getInstance(assetManager).mediumFont();
			add(Constants.UI.GRAY_BUTTON_TEXT, tbs);
		}
		{
			TextButtonStyle tbs = new TextButtonStyle();
			tbs.font = Fonts.getInstance(assetManager).mediumFont();
			add(Constants.UI.GREEN_BUTTON_TEXT, tbs);
		}
		{
			TextButtonStyle tbs = new TextButtonStyle();
			tbs.font = Fonts.getInstance(assetManager).xSmallFont();
			add(Constants.UI.GREEN_BUTTON_TEXT_SMALL, tbs);
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
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(gameBoardAtlas.findRegion("ship"));
			add("shipImage", trd, Drawable.class);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(gameBoardAtlas.findRegion("crosshairs"));
			add("crosshairs", trd, Drawable.class);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(gameBoardAtlas.findRegion("transfer"));
			add("transfer", trd, Drawable.class);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(gameBoardAtlas.findRegion("explosion_particle"));
			add(Constants.UI.EXPLOSION_PARTICLE, trd, Drawable.class);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(socialAtlas.findRegion("share_icon"));
			add(Constants.UI.SHARE_ICON, trd, Drawable.class);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(socialAtlas.findRegion("play_arrow"));
			add(Constants.UI.PLAY_ARROW, trd, Drawable.class);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("highlight_bar"));
			add(Constants.UI.HIGHLIGHT_BAR, trd, Drawable.class);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("xp_bar_cover"));
			add(Constants.UI.XP_BAR_COVER, trd, Drawable.class);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("xp_bar_main"));
			add(Constants.UI.XP_BAR_MAIN, trd, Drawable.class);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("xp_bar_arrow"));
			add(Constants.UI.XP_BAR_ARROW, trd, Drawable.class);
		}

		/*
		 * Image Buttons
		 */
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("wait"));
			add(Constants.UI.WAIT_BUTTON, new ImageButtonStyle(null, null, null, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("ok_button"));
			ImageButtonStyle okButtonStyle = new ImageButtonStyle(trd, trd, trd, trd, trd, trd);
			okButtonStyle.imageDisabled = new TextureRegionDrawable(menusAtlas.findRegion("ok_button_disabled"));
			add(Constants.UI.OK_BUTTON, okButtonStyle);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("question_mark"));
			add(Constants.UI.QUESTION_MARK, new ImageButtonStyle(null, null, null, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("dialog_bg"));
			add(Constants.UI.DIALOG_BG, new ImageButtonStyle(null, null, null, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("green_button"));
			add(Constants.UI.GREEN_BUTTON, new ImageButtonStyle(null, null, null, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("black_grey_button"));
			add(Constants.UI.GRAY_BUTTON, new ImageButtonStyle(null, null, null, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("button_yellow"));
			add(Constants.UI.BASIC_BUTTON, new ImageButtonStyle(trd, trd, trd, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("coin"));
			add(Constants.UI.COIN, new ImageButtonStyle(null, null, null, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(socialAtlas.findRegion("facebook_normal"));
			add(Constants.UI.FACEBOOK_SIGN_IN_BUTTON, new ImageButtonStyle(null, null, null, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(socialAtlas.findRegion("google_plus_custom_normal"));
			add(Constants.UI.GOOGLE_PLUS_SIGN_IN_NORMAL, new ImageButtonStyle(null, null, null, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(socialAtlas.findRegion("galcon_search_custom_normal"));
			add(Constants.UI.GALCON_SEARCH_IMAGE, new ImageButtonStyle(null, null, null, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("count_label"));
			add(Constants.UI.COUNT_LABEL, new ImageButtonStyle(null, null, null, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(levelSelectionAtlas.findRegion("scroll_highlight"));
			add(Constants.UI.SCROLL_HIGHLIGHT, trd, Drawable.class);
		}

		TextureRegionDrawable trd = new TextureRegionDrawable(levelSelectionAtlas.findRegion("reg_play"));
		add("regularPlay", new ImageButtonStyle(null, null, null, trd, trd, trd));

		trd = new TextureRegionDrawable(levelSelectionAtlas.findRegion("social_play"));
		add("socialPlay", new ImageButtonStyle(null, null, null, trd, trd, trd));

		trd = new TextureRegionDrawable(menusAtlas.findRegion("back"));
		add("backButton", new ImageButtonStyle(null, null, null, trd, trd, trd));

		trd = new TextureRegionDrawable(menusAtlas.findRegion("back_arrow_white"));
		add(Constants.UI.BACK_ARROW_WHITE, new ImageButtonStyle(null, null, null, trd, trd, trd));

		trd = new TextureRegionDrawable(menusAtlas.findRegion("cancel_button"));
		add("cancelButton", new ImageButtonStyle(null, null, null, trd, trd, trd));

		trd = new TextureRegionDrawable(menusAtlas.findRegion("ok_button"));
		ImageButtonStyle okButtonStyle = new ImageButtonStyle(trd, trd, trd, trd, trd, trd);
		okButtonStyle.imageDisabled = new TextureRegionDrawable(menusAtlas.findRegion("ok_button_disabled"));
		add("okButton", okButtonStyle);

		trd = new TextureRegionDrawable(socialAtlas.findRegion("Google+_chiclet_Red"));
		add("googlePlusButton", new ImageButtonStyle(null, null, null, trd, trd, trd));

		trd = new TextureRegionDrawable(menusAtlas.findRegion("end_turn"));
		add("performMoveButton", new ImageButtonStyle(null, null, null, trd, trd, trd));

		trd = new TextureRegionDrawable(gameBoardAtlas.findRegion("ship"));
		add("shipButton", new ImageButtonStyle(null, null, null, trd, trd, trd));

		trd = new TextureRegionDrawable(menusAtlas.findRegion("refresh"));
		add("refreshButton", new ImageButtonStyle(null, null, null, trd, trd, trd));

		trd = new TextureRegionDrawable(menusAtlas.findRegion("options"));
		add("optionsButton", new ImageButtonStyle(null, null, null, trd, trd, trd));
	}
}

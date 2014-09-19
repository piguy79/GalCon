package com.railwaygames.solarsmash;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class UISkin extends Skin {

	private AssetManager assetManager;

	@Override
	public BitmapFont getFont(String name) {
		if (name.equals(Constants.UI.X_LARGE_FONT)) {
			return Fonts.getInstance(assetManager).xLargeFont();
		} else if (name.equals(Constants.UI.LARGE_FONT)) {
			return Fonts.getInstance(assetManager).largeFont();
		} else if (name.equals(Constants.UI.X_SMALL_FONT)) {
			return Fonts.getInstance(assetManager).xSmallFont();
		} else if (name.equals(Constants.UI.MEDIUM_LARGE_FONT)) {
			return Fonts.getInstance(assetManager).mediumLargeFont();
		} else if (name.equals(Constants.UI.DEFAULT_FONT)) {
			return Fonts.getInstance(assetManager).mediumFont();
		} else if (name.equals(Constants.UI.SMALL_FONT)) {
			return Fonts.getInstance(assetManager).smallFont();
		}
		return super.getFont(name);
	}

	public void initialize(AssetManager assetManager) {
		this.assetManager = assetManager;

		TextureAtlas socialAtlas = assetManager.get("data/images/social.atlas", TextureAtlas.class);
		TextureAtlas levelSelectionAtlas = assetManager.get("data/images/levelSelection.atlas", TextureAtlas.class);
		TextureAtlas menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);

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
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(levelSelectionAtlas.findRegion("level_card_gray"));
			add(Constants.UI.LEVEL_CARD_BG, trd, Drawable.class);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("coin"));
			add(Constants.UI.COIN_IMAGE, trd, Drawable.class);
		}
		{
			NinePatchDrawable trd = new NinePatchDrawable(createNinePatch(menusAtlas.findRegion("button_gray")));
			add(Constants.UI.GRAY_IMAGE_BG, trd, Drawable.class);
		}

		/*
		 * Buttons
		 */
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("coin"));
			add(Constants.UI.COIN, new ButtonStyle(trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("back"));
			add("backButton", new ButtonStyle(trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("cancel_button"));
			add("cancelButton", new ButtonStyle(trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("ok_button"));
			ButtonStyle okButtonStyle = new ButtonStyle(trd, trd, trd);
			okButtonStyle.disabled = new TextureRegionDrawable(menusAtlas.findRegion("ok_button_disabled"));
			add("okButton", okButtonStyle);
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("end_turn"));
			add("performMoveButton", new ButtonStyle(trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("refresh"));
			add("refreshButton", new ButtonStyle(trd, trd, trd));
		}

		{
			TextureRegionDrawable trd = new TextureRegionDrawable(socialAtlas.findRegion("facebook_normal"));
			add(Constants.UI.FACEBOOK_SIGN_IN_BUTTON, new ButtonStyle(trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(socialAtlas.findRegion("google_plus_custom_normal"));
			add(Constants.UI.GOOGLE_PLUS_SIGN_IN_NORMAL, new ButtonStyle(trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(socialAtlas.findRegion("galcon_search_custom_normal"));
			add(Constants.UI.GALCON_SEARCH_IMAGE, new ButtonStyle(trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(levelSelectionAtlas.findRegion("level_card_tab_left"));
			add(Constants.UI.TAB_LEFT, new ButtonStyle(trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(
					levelSelectionAtlas.findRegion("level_card_tab_right"));
			add(Constants.UI.TAB_RIGHT, new ButtonStyle(trd, trd, trd));
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
			NinePatchDrawable trd = new NinePatchDrawable(createNinePatch(menusAtlas.findRegion("button_yellow")));
			add(Constants.UI.BASIC_BUTTON, new ButtonStyle(trd, trd, trd));
		}
		{
			NinePatchDrawable trd = new NinePatchDrawable(createNinePatch(menusAtlas.findRegion("button_clear")));
			add(Constants.UI.CLEAR_BUTTON, new ButtonStyle(trd, trd, trd));
		}
		{
			NinePatchDrawable trd = new NinePatchDrawable(createNinePatch(menusAtlas.findRegion("button_gray")));
			add(Constants.UI.GRAY_BUTTON, new ButtonStyle(trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("count_label"));
			add(Constants.UI.COUNT_LABEL, new ImageButtonStyle(null, null, null, trd, trd, trd));
		}
		{
			TextureRegionDrawable trd = new TextureRegionDrawable(levelSelectionAtlas.findRegion("scroll_highlight"));
			add(Constants.UI.SCROLL_HIGHLIGHT, trd, Drawable.class);
		}

		TextureRegionDrawable trd = new TextureRegionDrawable(menusAtlas.findRegion("back_arrow_white"));
		add(Constants.UI.BACK_ARROW_WHITE, new ImageButtonStyle(null, null, null, trd, trd, trd));

		trd = new TextureRegionDrawable(gameBoardAtlas.findRegion("ship"));
		add("shipButton", new ImageButtonStyle(null, null, null, trd, trd, trd));

		trd = new TextureRegionDrawable(menusAtlas.findRegion("options"));
		add("optionsButton", new ImageButtonStyle(null, null, null, trd, trd, trd));
	}

	private NinePatch createNinePatch(AtlasRegion region) {
		int[] splits = region.splits;
		NinePatch patch = null;
		if (splits != null) {
			patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
			int[] pads = ((AtlasRegion) region).pads;
			if (pads != null)
				patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
		}

		return patch;
	}
}

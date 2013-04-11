package com.xxx.galcon.screen;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.ScreenFeedback;

public class ShipSelectionDialog extends TouchRegion implements ScreenFeedback {
	private static final String OK = "ok";
	private static final String CANCEL = "cancel";
	private static final String UP = "up";
	private static final String DOWN = "down";

	private Map<String, TouchRegion> touchRegions = new HashMap<String, TouchRegion>();

	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private Texture dialogTexture;

	private Action returnResult;

	public ShipSelectionDialog(AssetManager assetManager) {
		super(20, 500, 500, 320, false);
		spriteBatch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_32.fnt"),
				Gdx.files.internal("data/fonts/tahoma_32.png"), false);

		dialogTexture = assetManager.get("data/images/ship_selection_dialog.png", Texture.class);

		touchRegions.put(OK, new TouchRegion(x + width * .5f, y + height * .1f, width * .4f, height * .25f, false));
		touchRegions.put(CANCEL, new TouchRegion(x + width * .1f, y + height * .1f, width * .4f, height * .25f, false));
		touchRegions.put(UP, new TouchRegion(x + width * .6f, y + height * .7f, width * .3f, height * .2f, false));
		touchRegions.put(DOWN, new TouchRegion(x + width * .6f, y + height * .4f, width * .3f, height * .2f, false));
	}

	@Override
	public void render(float delta) {
		processTouch();

		spriteBatch.begin();

		spriteBatch.draw(dialogTexture, x, y, width, height);

		spriteBatch.end();
	}

	private void processTouch() {
		returnResult = null;

		if (Gdx.input.justTouched()) {
			int x = Gdx.input.getX();
			int y = Gdx.graphics.getHeight() - Gdx.input.getY();

			for (Map.Entry<String, TouchRegion> entry : touchRegions.entrySet()) {
				if (entry.getValue().contains(x, y)) {
					if (entry.getKey().equals(OK)) {
						returnResult = Action.DIALOG_OK;
					} else if (entry.getKey().equals(CANCEL)) {
						returnResult = Action.DIALOG_CANCEL;
					}
				}
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getRenderResult() {
		return returnResult;
	}

	@Override
	public void resetState() {
		returnResult = null;
	}

}

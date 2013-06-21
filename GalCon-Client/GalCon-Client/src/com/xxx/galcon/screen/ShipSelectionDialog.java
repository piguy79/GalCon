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

	private int shipsToSend = 0;
	private int max;

	private Action returnResult;

	public ShipSelectionDialog(int x, int y, int width, int height, AssetManager assetManager, int max) {
		super(x, y, width, height, false);
		spriteBatch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_32.fnt"),
				Gdx.files.internal("data/fonts/tahoma_32.png"), false);

		dialogTexture = assetManager.get("data/images/ship_selection_dialog.png", Texture.class);

		touchRegions.put(OK, new TouchRegion(x + width * .5f, y + height * .1f, width * .4f, height * .25f, false));
		touchRegions.put(CANCEL, new TouchRegion(x + width * .1f, y + height * .1f, width * .4f, height * .25f, false));
		touchRegions.put(UP, new TouchRegion(x + width * .6f, y + height * .7f, width * .3f, height * .2f, false));
		touchRegions.put(DOWN, new TouchRegion(x + width * .6f, y + height * .4f, width * .3f, height * .2f, false));

		this.max = max;
	}

	@Override
	public void render(float delta) {
		processTouch();

		spriteBatch.begin();

		spriteBatch.draw(dialogTexture, x, y, width, height);
		font.draw(spriteBatch, "" + shipsToSend, x + width * .3f, y + height * .8f);
		font.draw(spriteBatch, "" + max, x + width * .3f, y + height * .48f);

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
					} else if (entry.getKey().equals(UP)) {
						shipsToSend++;
						shipsToSend = Math.min(shipsToSend, max);
					} else if (entry.getKey().equals(DOWN)) {
						shipsToSend--;
						shipsToSend = Math.max(shipsToSend, 0);
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

	public int getShipsToSend() {
		return shipsToSend;
	}

	@Override
	public void resetState() {
		returnResult = null;
	}

}

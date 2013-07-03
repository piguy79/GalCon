package com.xxx.galcon.screen;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.InGameInputProcessor.TouchPoint;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.tween.ShipSelectionDialogTween;

public class ShipSelectionDialog extends TouchRegion implements ScreenFeedback {
	private static final String OK = "ok";
	private static final String CANCEL = "cancel";
	private static final String UP = "up";
	private static final String DOWN = "down";

	private Map<String, UpdatingTouchRegion> touchRegions = new HashMap<String, UpdatingTouchRegion>();

	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private Texture dialogTexture;

	private int shipsToSend = 0;
	private Move currentMoveToEdit = null;
	private int max;

	private String returnResult;

	public Tween showAnimation;
	public Tween hideAnimation;
	private TweenManager tweenManager;

	public ShipSelectionDialog(Move currentMoveToEdit, int x, int y, int width, int height, AssetManager assetManager,
			int max, TweenManager tweenManager) {
		this(x, y, width, height, assetManager, max, tweenManager);
		if (currentMoveToEdit != null) {
			this.shipsToSend = currentMoveToEdit.shipsToMove;
			this.currentMoveToEdit = currentMoveToEdit;
		}
	}

	public ShipSelectionDialog(int x, int y, int width, int height, AssetManager assetManager, int max,
			TweenManager tweenManager) {
		super(x, y, width, height, false);
		this.tweenManager = tweenManager;

		int targetwidth = Gdx.graphics.getWidth();
		int targetheight = Gdx.graphics.getHeight();
		int xMargin = (int) (targetwidth * .15f);

		this.showAnimation = Tween.to(this, ShipSelectionDialogTween.POSITION_XY, 0.5f).target(xMargin,
				(int) (targetheight * .6f));
		TweenManager.setAutoStart(showAnimation, false);
		this.hideAnimation = Tween.to(this, ShipSelectionDialogTween.POSITION_XY, 0.3f).target(targetwidth * -1,
				(int) (targetheight * .6f));
		TweenManager.setAutoStart(hideAnimation, false);

		spriteBatch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/fonts/tahoma_32.fnt"),
				Gdx.files.internal("data/fonts/tahoma_32.png"), false);

		dialogTexture = assetManager.get("data/images/ship_selection_dialog.png", Texture.class);

		touchRegions.put(OK, new UpdatingTouchRegion(x, y, width * .4f, height * .25f, width, height, false) {

			@Override
			protected void updateToPoint(float x, float y) {
				this.x = x + startingWidth * 0.5f;
				;
				this.y = y + startingHeight * .1f;

			}
		});
		touchRegions.put(CANCEL, new UpdatingTouchRegion(x, y, width * .4f, height * .25f, width, height, false) {

			@Override
			protected void updateToPoint(float x, float y) {
				this.x = x + startingWidth * .1f;
				this.y = y + startingHeight * .1f;
			}
		});
		touchRegions.put(UP, new UpdatingTouchRegion(x, y, width * .3f, height * .2f, width, height, false) {

			@Override
			protected void updateToPoint(float x, float y) {
				this.x = x + startingWidth * .6f;
				this.y = y + startingHeight * .7f;
			}
		});
		touchRegions.put(DOWN, new UpdatingTouchRegion(x, y, width * .3f, height * .2f, width, height, false) {

			@Override
			protected void updateToPoint(float x, float y) {
				this.x = x + startingWidth * .6f;
				this.y = y + startingHeight * .4f;
			}
		});

		this.max = max;
	}

	public float getX() {
		return super.x;
	}

	public void setX(float x) {
		super.x = x;
	}

	public float getY() {
		return super.y;
	}

	public void setY(float y) {
		super.y = y;
	}

	@Override
	public void render(float delta) {
		processTouch();

		spriteBatch.begin();

		if (showAnimation != null && !showAnimation.isStarted()) {
			TweenManager.setAutoStart(showAnimation, true);

			showAnimation.start(tweenManager);
		}

		updateTouchRegions(x, y);

		spriteBatch.draw(dialogTexture, x, y, width, height);
		font.draw(spriteBatch, "" + shipsToSend, x + width * .3f, y + height * .8f);
		font.draw(spriteBatch, "" + max, x + width * .3f, y + height * .48f);

		spriteBatch.end();
	}

	private void updateTouchRegions(float x, float y) {
		for (Entry<String, UpdatingTouchRegion> entry : touchRegions.entrySet()) {
			UpdatingTouchRegion region = entry.getValue();
			region.updatePoint(x, y);
		}
	}

	private void processTouch() {
		returnResult = null;

		InGameInputProcessor ip = (InGameInputProcessor) Gdx.input.getInputProcessor();
		if (ip.didTouch() && showAnimation.isFinished()) {
			TouchPoint touchPoint = ip.getTouch();
			int x = touchPoint.x;
			int y = Gdx.graphics.getHeight() - touchPoint.y;

			for (Map.Entry<String, UpdatingTouchRegion> entry : touchRegions.entrySet()) {
				if (entry.getValue().contains(x, y)) {
					ip.consumeTouch();
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
		if (currentMoveToEdit != null && returnResult != null) {
			if (returnResult.equals(Action.DIALOG_OK)) {
				return Action.DIALOG_UPDATE;
			} else {
				return Action.DIALOG_DELETE;
			}
		}
		return returnResult;
	}

	public int getShipsToSend() {
		return shipsToSend;
	}

	@Override
	public void resetState() {

	}

	public void setCurrentMoveToEdit(Move currentMoveToEdit) {
		this.currentMoveToEdit = currentMoveToEdit;
	}

	public Move getCurrentMoveToEdit() {
		return currentMoveToEdit;
	}

}

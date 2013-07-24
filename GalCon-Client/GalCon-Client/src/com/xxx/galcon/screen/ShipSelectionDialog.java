package com.xxx.galcon.screen;

import java.util.ArrayList;
import java.util.List;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.InGameInputProcessor.TouchPoint;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.tween.ColorTween;
import com.xxx.galcon.model.tween.ShipSelectionDialogTween;
import com.xxx.galcon.screen.hud.Button;
import com.xxx.galcon.screen.hud.DragButton;

public class ShipSelectionDialog extends TouchRegion implements ScreenFeedback {
	private static final String OK = "ok";
	private static final String CANCEL = "cancel";
	private static final String DRAG = "drag";

	private List<Button> buttons = new ArrayList<Button>();
	private DragButton shipDragButton;

	private SpriteBatch spriteBatch;
	private Texture dialogTextureBg;
	private Texture shipTex;
	private Texture okButtonTex;
	private Texture cancelButtonTex;

	private int shipsToSend = 0;
	private Move currentMoveToEdit = null;
	private int max;
	private Color alphaAnimColor = new Color(1.0f, 1.0f, 1.0f, 0.0f);

	private String returnResult;
	private String pendingReturnResult;

	private Tween showAnimation;
	private Tween hideAnimation;
	private Tween showAlphaAnimation;
	private Tween hideAlphaAnimation;
	private TweenManager tweenManager;

	public ShipSelectionDialog(Move currentMoveToEdit, int x, int y, int width, int height, AssetManager assetManager,
			int max, TweenManager tweenManager) {
		this(x, y, width, height, assetManager, max, tweenManager);
		if (currentMoveToEdit != null) {
			this.shipsToSend = currentMoveToEdit.shipsToMove;
			this.currentMoveToEdit = currentMoveToEdit;
		}
	}

	public ShipSelectionDialog(int x, int y, int width, int height, AssetManager assetManager, final int max,
			TweenManager tweenManager) {
		super(x, y, width, height, false);
		this.tweenManager = tweenManager;

		int targetwidth = Gdx.graphics.getWidth();
		int targetheight = Gdx.graphics.getHeight();
		int xMargin = (int) (targetwidth * .1f);

		Tween.registerAccessor(Color.class, new ColorTween());

		this.showAnimation = Tween.to(this, ShipSelectionDialogTween.POSITION_XY, 0.3f)
				.target(xMargin, (int) (targetheight * .6f)).ease(TweenEquations.easeOutQuad);
		this.hideAnimation = Tween.to(this, ShipSelectionDialogTween.POSITION_XY, 0.3f)
				.target(targetwidth * -1, (int) (targetheight * .6f)).ease(TweenEquations.easeInQuad);
		this.showAlphaAnimation = Tween.to(this.alphaAnimColor, ColorTween.ALPHA, 0.2f).target(1.0f, 1.0f, 1.0f, 1.0f);
		this.hideAlphaAnimation = Tween.to(this.alphaAnimColor, ColorTween.ALPHA, 0.2f).target(1.0f, 1.0f, 1.0f, 0.0f);

		spriteBatch = new SpriteBatch();

		okButtonTex = assetManager.get("data/images/ok_button.png", Texture.class);
		cancelButtonTex = assetManager.get("data/images/cancel_button.png", Texture.class);
		dialogTextureBg = assetManager.get("data/images/ship_selection_dialog_bg.png", Texture.class);
		shipTex = assetManager.get("data/images/ship.png", Texture.class);

		buttons.add(new Button(okButtonTex) {
			@Override
			public void updateLocationAndSize(int x, int y, int width, int height) {
				int buttonSize = (int) (width * 0.15f);
				int margin = 0;

				this.x = x + width - margin - buttonSize;
				this.y = y + margin;
				this.height = buttonSize;
				this.width = buttonSize;
			}

			@Override
			public String getActionOnClick() {
				return OK;
			}
		});

		buttons.add(new Button(cancelButtonTex) {
			@Override
			public void updateLocationAndSize(int x, int y, int width, int height) {
				int buttonSize = (int) (width * 0.15f);
				int margin = 0;

				this.x = x + margin;
				this.y = y + margin;
				this.height = buttonSize;
				this.width = buttonSize;
			}

			@Override
			public String getActionOnClick() {
				return CANCEL;
			}
		});

		shipDragButton = new DragButton(shipTex) {

			@Override
			public void updateLocationAndSize(int x, int y, int width, int height) {
				int buttonSize = (int) (width * 0.1f);

				int dragWidth = maxX - minX;
				this.x = (int) (minX + ((float) shipsToSend / (float) max) * dragWidth);
				this.y = y + height - buttonSize - (int) (height * 0.30f);
				this.height = buttonSize;
				this.width = buttonSize;
			}

			@Override
			public void updateDragBounds(int x, int y, int width, int height) {
				this.minX = x + (int) (width * 0.05f);
				this.maxX = x + width - 2 * (int) (width * 0.08f);
			}

			@Override
			public String getActionOnClick() {
				return DRAG;
			}
		};
		buttons.add(shipDragButton);

		this.max = max;
	}

	public boolean isReady() {
		return this.showAnimation.isFinished() && !this.hideAnimation.isStarted();
	}

	public float getX() {
		return super.x;
	}

	public void setX(int x) {
		super.x = x;
	}

	public float getY() {
		return super.y;
	}

	public void setY(int y) {
		super.y = y;
	}

	private boolean buttonsUpdated = false;

	@Override
	public void render(float delta) {
		processTouch();

		spriteBatch.begin();

		if (!showAnimation.isStarted()) {
			showAnimation.start(tweenManager);
		}

		spriteBatch.draw(dialogTextureBg, x, y, width, height);

		if (showAnimation.isFinished()) {
			if (!showAlphaAnimation.isStarted()) {
				showAlphaAnimation.start(tweenManager);
			}

			spriteBatch.setColor(alphaAnimColor);
			for (int i = 0; i < buttons.size(); ++i) {
				Button button = buttons.get(i);

				if (!buttonsUpdated) {
					if (button instanceof DragButton) {
						((DragButton) button).updateDragBounds(x, y, width, height);
					}
					button.updateLocationAndSize(x, y, width, height);
				}

				button.render(spriteBatch);
			}
			buttonsUpdated = true;
			spriteBatch.setColor(Color.WHITE);
		}

		if (shipDragButton.isDragging()) {
			float ratio = shipDragButton.getDragRatio();
			shipsToSend = (int) Math.ceil(ratio * max);
		}

		BitmapFont font = Fonts.getInstance().largeFont();
		font.setColor(Color.BLACK);
		font.draw(spriteBatch, "" + (max - shipsToSend), x + width * .38f, y + height * .84f);
		font.draw(spriteBatch, ">>", x + width * .48f, y + height * .84f);
		font.draw(spriteBatch, "" + shipsToSend, x + width * .58f, y + height * .84f);
		font.setColor(Color.WHITE);

		spriteBatch.end();
	}

	private void processTouch() {
		returnResult = null;

		InGameInputProcessor ip = (InGameInputProcessor) Gdx.input.getInputProcessor();
		if (ip.didTouch() && showAnimation.isFinished()) {
			TouchPoint touchPoint = ip.getTouch();
			int x = touchPoint.x;
			int y = Gdx.graphics.getHeight() - touchPoint.y;

			for (int i = 0; i < buttons.size(); ++i) {
				Button button = buttons.get(i);
				if (button.isTouched(x, y)) {
					ip.consumeTouch();
					if (button.getActionOnClick().equals(OK)) {
						pendingReturnResult = Action.DIALOG_OK;
						hideAlphaAnimation.start(tweenManager);
					} else if (button.getActionOnClick().equals(CANCEL)) {
						pendingReturnResult = Action.DIALOG_CANCEL;
						hideAlphaAnimation.start(tweenManager);
					}
				}
			}
		}

		if (pendingReturnResult != null && hideAlphaAnimation.isFinished()) {
			if (!hideAnimation.isStarted()) {
				hideAnimation.start(tweenManager);
			}
		}

		if (pendingReturnResult != null && hideAnimation.isFinished()) {
			returnResult = pendingReturnResult;
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
		if (!hideAnimation.isStarted()) {
			hideAnimation.start(tweenManager);
		}
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

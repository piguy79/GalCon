package com.xxx.galcon.screen;

import static aurelienribon.tweenengine.TweenCallback.COMPLETE;
import static aurelienribon.tweenengine.TweenCallback.END;

import java.util.ArrayList;
import java.util.List;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
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

	private static final String TWEEN_ID_SHOW = "show";
	private static final String TWEEN_ID_BUTTON_SHOW = "button show";
	private static final String TWEEN_ID_HIDE = "hide";
	private static final String TWEEN_ID_BUTTON_HIDE = "button hide";

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

	private TweenManager tweenManager;
	private boolean isBaseDialogReady = false;
	private boolean isReady = false;
	private boolean isShownAndClosed = false;

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

		Tween.registerAccessor(Color.class, new ColorTween());

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

				float ratio = 0.0f;
				if (max != 0) {
					ratio = ((float) shipsToSend / (float) max);
				}
				this.x = (int) (minX + ratio * dragWidth);
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
		return isReady;
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

		spriteBatch.draw(dialogTextureBg, x, y, width, height);

		if (isBaseDialogReady) {
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
		if (ip.didTouch() && isReady) {
			TouchPoint touchPoint = ip.getTouch();
			int x = touchPoint.x;

			for (int i = 0; i < buttons.size(); ++i) {
				Button button = buttons.get(i);
				if (button.isTouched(x, y)) {
					ip.consumeTouch();
					if (button.getActionOnClick().equals(OK)) {
						pendingReturnResult = Action.DIALOG_OK;
						hide();
					} else if (button.getActionOnClick().equals(CANCEL)) {
						pendingReturnResult = Action.DIALOG_CANCEL;
						hide();
					}
				}
			}
		}

		if (pendingReturnResult != null && isShownAndClosed) {
			returnResult = pendingReturnResult;
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		int targetwidth = Gdx.graphics.getWidth();
		int targetheight = Gdx.graphics.getHeight();
		int xMargin = (int) (targetwidth * .1f);

		ShowDialogCallback cb = new ShowDialogCallback();

		Tween showAnimation = Tween.to(this, ShipSelectionDialogTween.POSITION_XY, 0.3f)
				.target(xMargin, (int) (targetheight * .6f)).ease(TweenEquations.easeOutQuad)
				.setUserData(TWEEN_ID_SHOW).setCallback(cb).setCallbackTriggers(END);
		Tween showButtonAnimation = Tween.to(this.alphaAnimColor, ColorTween.ALPHA, 0.2f)
				.target(1.0f, 1.0f, 1.0f, 1.0f).setUserData(TWEEN_ID_BUTTON_SHOW).setCallback(cb)
				.setCallbackTriggers(END);

		Timeline.createSequence().push(showAnimation).push(showButtonAnimation).start(tweenManager);
	}

	@Override
	public void hide() {
		int targetwidth = Gdx.graphics.getWidth();
		int targetheight = Gdx.graphics.getHeight();

		Tween hideAlphaAnimation = Tween.to(this.alphaAnimColor, ColorTween.ALPHA, 0.2f).target(1.0f, 1.0f, 1.0f, 0.0f)
				.setUserData(TWEEN_ID_BUTTON_HIDE);

		Tween hideAnimation = Tween.to(this, ShipSelectionDialogTween.POSITION_XY, 0.3f)
				.target(targetwidth * -1, (int) (targetheight * .6f)).ease(TweenEquations.easeInQuad)
				.setUserData(TWEEN_ID_HIDE);

		Timeline.createSequence().push(hideAlphaAnimation).push(hideAnimation).setCallback(new HideDialogCallback())
				.setCallbackTriggers(COMPLETE).start(tweenManager);
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
		hide();
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

	private class ShowDialogCallback implements TweenCallback {

		@Override
		public void onEvent(int type, BaseTween<?> source) {
			switch (type) {
			case END:
				if (source.getUserData().equals(TWEEN_ID_SHOW)) {
					isBaseDialogReady = true;
				} else if (source.getUserData().equals(TWEEN_ID_BUTTON_SHOW)) {
					isReady = true;
				}
				break;
			default:
				break;
			}

		}
	}

	private class HideDialogCallback implements TweenCallback {

		@Override
		public void onEvent(int type, BaseTween<?> source) {
			switch (type) {
			case COMPLETE:
				isShownAndClosed = true;
				break;
			default:
				break;
			}

		}
	}
}

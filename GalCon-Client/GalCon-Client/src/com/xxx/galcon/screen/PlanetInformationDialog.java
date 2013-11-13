/**
 * 
 */
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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.tween.ColorTween;
import com.xxx.galcon.model.tween.PlanetInformationTween;
import com.xxx.galcon.screen.hud.Button;

/**
 * @author conormullen
 *
 */
public class PlanetInformationDialog extends TouchRegion implements ScreenFeedback {
	
	private Planet planet;
	private TweenManager tweenManager;
	
	private Texture cancelButtonTex;
	private Texture dialogTextureBg;
	private List<Button> buttons = new ArrayList<Button>();
	
	SpriteBatch spriteBatch;
	
	private static final String CANCEL = "cancel";
	
	private boolean isBaseDialogReady = false;
	private boolean isReady = false;
	private boolean isShownAndClosed = false;
	
	private static final String TWEEN_ID_SHOW = "show";
	private static final String TWEEN_ID_BUTTON_SHOW = "button show";
	private static final String TWEEN_ID_HIDE = "hide";
	private static final String TWEEN_ID_BUTTON_HIDE = "button hide";
	
	private Color alphaAnimColor = new Color(1.0f, 1.0f, 1.0f, 0.0f);



	public PlanetInformationDialog(int x, int y, int width, int height,
			 Planet planet, AssetManager assetManager, TweenManager tweenManager) {
		super(x, y, width, height, false);
		this.planet = planet;
		this.tweenManager = tweenManager;
		spriteBatch = new SpriteBatch();
		
		cancelButtonTex = assetManager.get("data/images/cancel_button.png", Texture.class);
		dialogTextureBg = assetManager.get("data/images/ship_selection_dialog_bg.png", Texture.class);
		
		Tween.registerAccessor(Color.class, new ColorTween());
		
		addCancelButton();
	}

	private void addCancelButton() {
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

	@Override
	public void render(float delta) {
		spriteBatch.begin();
		spriteBatch.draw(dialogTextureBg, x, y, width, height);
		
		if(isBaseDialogReady){
			for(int i = 0; i < buttons.size(); i++){
				Button b = buttons.get(i);
				b.render(spriteBatch);
			}
		}
		
		spriteBatch.end();
		
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

		Tween showAnimation = Tween.to(this, PlanetInformationTween.POSITION_XY, 0.3f)
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

		Tween hideAnimation = Tween.to(this, PlanetInformationTween.POSITION_XY, 0.3f)
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetState() {
		// TODO Auto-generated method stub
		
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

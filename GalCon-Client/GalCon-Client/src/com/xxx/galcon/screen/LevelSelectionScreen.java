package com.xxx.galcon.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.InGameInputProcessor.TouchPoint;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Map;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.screen.hud.Button;
import com.xxx.galcon.screen.hud.Hud;
import com.xxx.galcon.screen.hud.LevelSelectionHud;

public class LevelSelectionScreen implements ScreenFeedback, UIConnectionResultCallback<Maps> {

	private List<Map> allMaps;
	private java.util.Map<Integer, Texture> mapTextures = new HashMap<Integer, Texture>();
	private int selectedMap = 0;

	private SpriteBatch spriteBatch;
	private AssetManager assetManager;

	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();

	private String loadingMessage = "Loading...";

	private Texture levelSelectBg;
	private Texture levelSelectBgBottom;
	private Texture levelSelectionCard;
	private Texture back;
	private Texture forward;
	private Texture regularPlay;
	private Texture socialPlay;

	private List<Button> buttons = new ArrayList<Button>();

	private Object returnValue;

	private Hud levelSelectionHud;

	public LevelSelectionScreen(AssetManager assetManager) {
		this.assetManager = assetManager;
		this.spriteBatch = new SpriteBatch();

		this.levelSelectBg = assetManager.get("data/images/level_select_bg.png", Texture.class);
		this.levelSelectBgBottom = assetManager.get("data/images/level_select_bg_bottom.png", Texture.class);
		this.levelSelectionCard = assetManager.get("data/images/level_card_black.png", Texture.class);
		this.forward = assetManager.get("data/images/arrow_right_small_black.png", Texture.class);
		this.back = assetManager.get("data/images/arrow_left_small_black.png", Texture.class);
		this.regularPlay = assetManager.get("data/images/reg_play.png", Texture.class);
		this.socialPlay = assetManager.get("data/images/social_play.png", Texture.class);

		levelSelectionHud = new LevelSelectionHud(assetManager);

		buttons.add(new Button(regularPlay) {
			@Override
			public void updateLocationAndSize(int x, int y, int width, int height) {
				int buttonWidth = (int) (height * 0.15f);
				int buttonHeight = (int) (height * 0.15f);
				int margin = (int) (width * 0.1f);
				int ymargin = (int)(height * 0.02f);


				this.x = x + margin;
				this.y = y + ymargin;
				this.height = buttonHeight;
				this.width = buttonWidth;
			}

			@Override
			public String getActionOnClick() {
				return Action.PLAY;
			}

			@Override
			public void render(SpriteBatch spriteBatch) {
				super.render(spriteBatch);
			}
		});

		buttons.add(new Button(socialPlay) {
			@Override
			public void updateLocationAndSize(int x, int y, int width, int height) {
				int buttonWidth = (int) (height * 0.15f);
				int buttonHeight = (int) (height * 0.15f);
				int xmargin = (int) (width * 0.1f);
				int ymargin = (int)(height * 0.02f);

				this.x = x + width - xmargin - buttonWidth;
				this.y = y + ymargin;
				this.height = buttonHeight;
				this.width = buttonWidth;
			}

			@Override
			public String getActionOnClick() {
				return Action.PLAY_WITH_FRIENDS;
			}

			@Override
			public void render(SpriteBatch spriteBatch) {
				super.render(spriteBatch);

			}
		});
	}

	private boolean buttonsUpdated = false;

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		float width = Gdx.graphics.getWidth() / 2;
		float height = Gdx.graphics.getHeight() / 2;

		viewMatrix.setToOrtho2D(0, 0, width, height);

		spriteBatch.setProjectionMatrix(viewMatrix);
		spriteBatch.setTransformMatrix(transformMatrix);
		spriteBatch.begin();
		spriteBatch.enableBlending();

		BitmapFont largeFont = Fonts.getInstance().mediumFont();
		BitmapFont mediumFont = Fonts.getInstance().mediumFont();
		BitmapFont smallFont = Fonts.getInstance().smallFont();

		if (allMaps == null) {
			BitmapFont font = mediumFont;
			if (loadingMessage.length() > 15) {
				font = smallFont;
			}
			float halfFontWidth = font.getBounds(loadingMessage).width / 2;
			font.draw(spriteBatch, loadingMessage, width / 2 - halfFontWidth, height * .4f);
		} else {
			
			spriteBatch.draw(levelSelectBg, 0, 0, width,
					height);
			
			spriteBatch.draw(levelSelectBgBottom, 0, 0, width,
					(int)height * 0.18f);
			
			largeFont.setColor(Color.WHITE);
			String text = "Choose Your Galaxy";
			float halfFontWidth = largeFont.getBounds(text).width / 2;
			largeFont.draw(spriteBatch, text, (width / 2 - halfFontWidth) + (halfFontWidth * 0.2f), height * .97f);
			largeFont.setColor(Color.WHITE);

			int cardHeight = (int) (height * .6f);
			int cardWidth = (int) (width * .7f);
			spriteBatch.draw(levelSelectionCard, width / 2 - cardWidth / 2, height * .28f, cardWidth,
					cardHeight);

			Map map = allMaps.get(selectedMap);

			text = map.title;
			mediumFont.setColor(Color.WHITE);
			halfFontWidth = mediumFont.getBounds(text).width / 2;
			mediumFont.draw(spriteBatch, text, width / 2 - halfFontWidth, height * .83f);
			mediumFont.setColor(Color.WHITE);

			text = map.description;
			smallFont.setColor(Color.WHITE);
			halfFontWidth = smallFont.getBounds(text).width / 2;
			smallFont.draw(spriteBatch, text, width / 2 - halfFontWidth, height * .38f);
			smallFont.setColor(Color.WHITE);

			Texture mapTex = mapTextures.get(map.key);
			int mapHeight = (int) (height * .35f);
			int mapWidth = (int) (width * .7f);
			spriteBatch.draw(mapTex, width / 2 - mapWidth / 2, height * .4f, mapWidth, mapHeight);

			int arrowWidth = (int) (width * .05f);
			int arrowHeight = (int) (arrowWidth * 1.5f);
			spriteBatch.draw(back, (int) (width * 0.05f), height / 2 - arrowHeight / 2, arrowWidth, arrowHeight);
			spriteBatch.draw(forward, (int) (width * 0.9f), height / 2 - arrowHeight / 2, arrowWidth, arrowHeight);

			for (int i = 0; i < buttons.size(); ++i) {
				Button button = buttons.get(i);

				if (!buttonsUpdated) {
					button.updateLocationAndSize(0, 0, (int) width, (int) height);
				}

				button.render(spriteBatch);
			}

			buttonsUpdated = true;
		}

		spriteBatch.end();

		levelSelectionHud.render(delta);
		if (levelSelectionHud.getRenderResult() != null) {
			String result = (String) levelSelectionHud.getRenderResult();
			if (result == Action.BACK) {
				returnValue = result;
			}
		}

		InGameInputProcessor ip = (InGameInputProcessor) Gdx.input.getInputProcessor();
		if (ip.didTouch()) {
			TouchPoint tp = ip.getTouch();
			int x = tp.x / 2;
			int y = tp.y / 2;
			for (int i = 0; i < buttons.size(); ++i) {
				Button button = buttons.get(i);
				if (button.isTouched(x, y)) {
					returnValue = button.getActionOnClick();
					ip.consumeTouch();
				}
			}
		}

		if (((InGameInputProcessor) Gdx.input.getInputProcessor()).isBackKeyPressed()) {
			returnValue = Action.BACK;
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		if (allMaps == null) {
			UIConnectionWrapper.findAllMaps(this);
		}
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
		return returnValue;
	}

	@Override
	public void resetState() {
		returnValue = null;
	}

	@Override
	public void onConnectionResult(Maps result) {
		this.allMaps = result.allMaps;

		for (Map map : allMaps) {
			mapTextures.put(map.key, assetManager.get("data/images/levels/" + map.key + ".png", Texture.class));
		}
	}

	@Override
	public void onConnectionError(String msg) {
		// TODO Auto-generated method stub

	}

}

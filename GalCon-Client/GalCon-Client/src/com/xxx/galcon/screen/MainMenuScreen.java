package com.xxx.galcon.screen;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.xxx.galcon.Constants;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.SetPlayerResultHandler;

public class MainMenuScreen implements ScreenFeedback {
	private SpriteBatch spriteBatch;
	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();
	private String returnValue;
	private GameAction gameAction;
	private Stage stage;
	private GameLoop gameLoop;

	private Image loadingFrame;
	private Image loadingBarHidden;
	private Image loadingBg;

	private float startX, endX;
	private float percent;

	private Actor loadingBar;

	Map<String, TouchRegion> touchRegions = new HashMap<String, TouchRegion>();

	public MainMenuScreen(GameLoop gameLoop, GameAction gameAction) {
		this.gameLoop = gameLoop;
		this.gameAction = gameAction;
	}

	private void addElementsToStage() {

		stage = new Stage();
		gameLoop.assetManager.load("data/images/loading.pack", TextureAtlas.class);
		gameLoop.assetManager.finishLoading();

		TextureAtlas atlas = gameLoop.assetManager.get("data/images/loading.pack", TextureAtlas.class);

		loadingFrame = new Image(atlas.findRegion("loading-frame"));
		loadingBarHidden = new Image(atlas.findRegion("loading-bar-hidden"));
		loadingBg = new Image(atlas.findRegion("loading-frame-bg"));
		loadingBar = new Image(atlas.findRegion("loading-bar-anim"));

		stage.addActor(loadingBar);
		stage.addActor(loadingBg);
		stage.addActor(loadingBarHidden);
		stage.addActor(loadingFrame);

	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
	}

	private void updateFont() {
		int width = Gdx.graphics.getWidth() / 2;
		int height = Gdx.graphics.getHeight() / 2;

		addText(Constants.JOIN, (int) (height * .4f), true, width, height);
		addText(Constants.CREATE, (int) (height * .31f), true, width, height);
		addText(Constants.CURRENT, (int) (height * .22f), true, width, height);
	}

	private String currentUserText() {
		return "Level " + GameLoop.USER.rank.level;
	}

	private void addText(String text, int y, boolean isTouchable, int screenWidth, int screenHeight) {
		BitmapFont font = Fonts.getInstance().largeFont();
		TextBounds fontBounds = font.getBounds(text);
		int x = screenWidth / 2 - (int) fontBounds.width / 2;
		font.draw(spriteBatch, text, x, y);

		if (isTouchable && touchRegions.size() != 3) {
			touchRegions.put(text, new TouchRegion(x, y, fontBounds.width, fontBounds.height, true));
		}
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		int width = Gdx.graphics.getWidth() / 2;
		int height = Gdx.graphics.getHeight() / 2;

		Integer touchX = null;
		Integer touchY = null;
		if (Gdx.input.isTouched()) {
			int x = Gdx.input.getX() / 2;
			int y = Gdx.input.getY() / 2;

			y = (int) height - y;
			touchX = x;
			touchY = y;
		}

		viewMatrix.setToOrtho2D(0, 0, width, height);
		spriteBatch.setProjectionMatrix(viewMatrix);
		spriteBatch.setTransformMatrix(transformMatrix);
		spriteBatch.begin();

		if (touchX != null) {
			for (Map.Entry<String, TouchRegion> touchRegionEntry : touchRegions.entrySet()) {
				TouchRegion touchRegion = touchRegionEntry.getValue();
				if (touchRegion.contains(touchX, touchY)) {
					returnValue = touchRegionEntry.getKey();
				}
			}
		}

		updateFont();

		String galcon = "GalCon";
		BitmapFont extraLargeFont = Fonts.getInstance().extraLargeFont();
		int x = width / 2 - (int) extraLargeFont.getBounds(galcon).width / 2;
		extraLargeFont.draw(spriteBatch, galcon, x, (int) (height * .9f));

		BitmapFont smallFont = Fonts.getInstance().smallFont();
		if (hasUserInformation()) {
			BitmapFont mediumFont = Fonts.getInstance().mediumFont();
			mediumFont.setColor(0.2f, 1.0f, 0.2f, 1.0f);
			smallFont.setColor(0.2f, 1.0f, 0.2f, 1.0f);
			x = width / 2 - (int) mediumFont.getBounds(currentUserText()).width / 2;
			mediumFont.draw(spriteBatch, currentUserText(), x, (int) (height * .8f));

			String toNextLevel = "To Next Level..." + (GameLoop.USER.rank.endAt - GameLoop.USER.xp + "xp");
			x = width / 2 - (int) smallFont.getBounds(toNextLevel).width / 2;
			smallFont.draw(spriteBatch, toNextLevel, x, (int) (height * .76f));

			spriteBatch.end();

			// Interpolate the percentage to make it more smooth
			float percentToUse = 0;
			if (GameLoop.USER.xp == 0) {
				percentToUse = 0f;
			} else {
				percentToUse = (float) ((float) GameLoop.USER.xp / (float) GameLoop.USER.rank.endAt);
			}

			percent = Interpolation.linear.apply(percent, percentToUse, 0.1f);

			// Update positions (and size) to match the percentage
			loadingBarHidden.setX(startX + endX * percent);
			loadingBg.setX(loadingBarHidden.getX() + 30);
			loadingBg.setWidth(450 - 450 * percent);
			loadingBg.invalidate();

			stage.draw();
		} else {
			String loadingUserInfo = "Loading User Information...";
			x = width / 2 - (int) smallFont.getBounds(loadingUserInfo).width / 2;
			smallFont.draw(spriteBatch, loadingUserInfo, x, (int) (height * .6f));

			spriteBatch.end();
		}
	}

	private boolean hasUserInformation() {
		return GameLoop.USER.rank != null;
	}

	@Override
	public void resize(int width, int height) {

		touchRegions.clear();
		updateRankProgressBar(width, height);
	}

	private void updateRankProgressBar(int width, int height) {
		int gdxHeight = Gdx.graphics.getHeight();

		width = gdxHeight * width / height;
		height = gdxHeight;
		stage.setViewport(width, height + 300, false);

		loadingFrame.setX((stage.getWidth() - loadingFrame.getWidth()) / 2);
		loadingFrame.setY((int) (gdxHeight * 0.82f));

		loadingBar.setX(loadingFrame.getX() + 15);
		loadingBar.setY(loadingFrame.getY() + 5);

		loadingBarHidden.setX(loadingBar.getX() + 35);
		loadingBarHidden.setY(loadingBar.getY() - 3);

		// The start position and how far to move the hidden loading bar
		startX = loadingBarHidden.getX();
		endX = 440;

		// The rest of the hidden bar
		loadingBg.setSize(450, 50);
		loadingBg.setX(loadingBarHidden.getX() + 30);
		loadingBg.setY(loadingBarHidden.getY() + 3);

	}

	@Override
	public void show() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		addElementsToStage();
		spriteBatch = new SpriteBatch();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() {

	}

	@Override
	public void resetState() {
		returnValue = null;
		try {
			gameAction.findUserInformation(new SetPlayerResultHandler(GameLoop.USER), GameLoop.USER.name);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object getRenderResult() {
		return returnValue;
	}

}

package com.xxx.galcon.screen;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
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
	private BitmapFontCache fontCache;
	private BitmapFont extraLargeFont;
	private BitmapFont smallFont;
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
		BitmapFont font = Fonts.getInstance().largeFont();
		fontCache = new BitmapFontCache(font);

		extraLargeFont = Fonts.getInstance().extraLargeFont();
		smallFont = Fonts.getInstance().smallFont();

		spriteBatch = new SpriteBatch();

		updateFont();
		
		stage = new Stage();
		addElementsToStage();
	}

	private void addElementsToStage() {
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
		fontCache.getFont().dispose();
		spriteBatch.dispose();
	}

	private void updateFont() {
		int width = Gdx.graphics.getWidth() / 2;
		int height = Gdx.graphics.getHeight() / 2;

		fontCache.clear();
		touchRegions.clear();

		addText(Constants.JOIN, (int) (height * .4f), true, width, height);
		addText(Constants.CREATE, (int) (height * .31f), true, width, height);
		addText(Constants.CURRENT, (int) (height * .22f), true, width, height);

	}

	private String currentUserText() {
		return "(Level " + GameLoop.USER.rank + ")";
	}

	private void addText(String text, int y, boolean isTouchable, int screenWidth, int screenHeight) {
		TextBounds fontBounds = fontCache.getFont().getBounds(text);
		int x = screenWidth / 2 - (int) fontBounds.width / 2;
		fontCache.addText(text, x, y);

		if (isTouchable) {
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

		fontCache.draw(spriteBatch);

		String galcon = "GalCon";
		int x = width / 2 - (int) extraLargeFont.getBounds(galcon).width / 2;
		extraLargeFont.draw(spriteBatch, galcon, x, (int) (height * .7f));
		
		if(hasUserInformation()){
			x = width / 2 - (int) smallFont.getBounds(currentUserText()).width / 2;
			smallFont.draw(spriteBatch, currentUserText(), x, (int) (height * .6f));
			
			// Interpolate the percentage to make it more smooth
	        percent = Interpolation.linear.apply(percent, .6f, 0.1f);

	        // Update positions (and size) to match the percentage
	        loadingBarHidden.setX(startX + endX * percent);
	        loadingBg.setX(loadingBarHidden.getX() + 30);
	        int subractionWidth = (int)(Gdx.graphics.getWidth() * 0.2f);
	        loadingBg.setWidth(subractionWidth - subractionWidth * percent);
	        loadingBg.setHeight((int)(Gdx.graphics.getHeight() * .05f));
	        loadingBg.invalidate();
			
	        stage.draw();
	        
	        
			
		}else {
			String loadingUserInfo = "Loading User Information...";
			x = width / 2 - (int) smallFont.getBounds(loadingUserInfo).width / 2;
			smallFont.draw(spriteBatch, loadingUserInfo, x, (int) (height * .6f));
		}
		
		spriteBatch.end();
		
	}

	private boolean hasUserInformation() {
		return GameLoop.USER.rank != null;
	}

	@Override
	public void resize(int width, int height) {
		updateFont();
		updateRankProgressBar();
	}

	private void updateRankProgressBar() {
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		
        stage.setViewport((int)(width * .2f),(int)(height * .9f), false);


        // Place the loading frame in the middle of the screen
        loadingFrame.setX(0);
        loadingFrame.setY((int) (height * .45f));
        loadingFrame.setWidth((int)(width * .2f));
        loadingFrame.setHeight((int)(height * .02f));

        // Place the loading bar at the same spot as the frame, adjusted a few px
        loadingBar.setX(loadingFrame.getX() + 5);
        loadingBar.setY(loadingFrame.getY()+5);
        loadingBar.setSize(loadingFrame.getWidth(), loadingFrame.getHeight() - 10);

        // Place the image that will hide the bar on top of the bar, adjusted a few px
        loadingBarHidden.setX(loadingBar.getX() + 35);
        loadingBarHidden.setY(loadingBar.getY() - 3);
        loadingBarHidden.setSize(loadingFrame.getWidth(), loadingFrame.getHeight());

        // The start position and how far to move the hidden loading bar
        startX = loadingBarHidden.getX();
        endX = (int)(width * .2f);

        // The rest of the hidden bar
        //loadingBg.setSize((int)(width * .2f), (int) (height * .01f));
        loadingBg.setSize(loadingFrame.getWidth(), loadingFrame.getHeight());
        loadingBg.setX(loadingBarHidden.getX() + 30);
        loadingBg.setY(loadingBarHidden.getY() + 3);
	}

	@Override
	public void show() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
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

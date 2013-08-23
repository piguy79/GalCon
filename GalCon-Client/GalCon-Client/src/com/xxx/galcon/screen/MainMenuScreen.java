package com.xxx.galcon.screen;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.xxx.galcon.Constants;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.InGameInputProcessor.TouchPoint;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.Strings;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.GooglePlusSignInListener;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.http.SocialAction;

public class MainMenuScreen implements ScreenFeedback, GooglePlusSignInListener {
	private SpriteBatch spriteBatch;
	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();
	private String returnValue;
	private GameAction gameAction;
	private SocialAction socialAction;
	private Stage stage;
	private GameLoop gameLoop;

	private Image loadingFrame;
	private Image loadingBarHidden;
	private Image loadingBg;

	private float startX, endX;
	private float percent;

	private Actor loadingBar;
	private ImageButton googlePlus;
	private Label googlePlusText;
	private Table googlePlusTable;
	private Label signInLabel;

	private Skin skin;

	Map<String, TouchRegion> touchRegions = new HashMap<String, TouchRegion>();

	public MainMenuScreen(GameLoop gameLoop, Skin skin, GameAction gameAction, SocialAction socialAction) {
		this.gameLoop = gameLoop;
		this.gameAction = gameAction;
		this.socialAction = socialAction;
		this.skin = skin;

		socialAction.registerGooglePlusSignInListener(this);
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

		googlePlus = new ImageButton(skin, "googlePlusButton");
		googlePlus.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				if (socialAction.isLoggedInToGooglePlus()) {
					socialAction.googlePlusSignOut();
				} else {
					socialAction.googlePlusSignIn();
				}
			}
		});

		googlePlusTable = new Table(skin);

		stage.addActor(loadingBar);
		stage.addActor(loadingBg);
		stage.addActor(loadingBarHidden);
		stage.addActor(loadingFrame);

		stage.addActor(googlePlusTable);
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
	}

	private void updateFont() {
		int width = Gdx.graphics.getWidth() / 2;
		int height = Gdx.graphics.getHeight() / 2;

		addText(Constants.New, (int) (height * .44f), true, width, height);
		addText(Constants.CONTINUE, (int) (height * .34f), true, width, height);

		if (socialAction.isLoggedInToGooglePlus()) {
			addText(Constants.LEADERBOARDS, (int) (height * .24f), true, width, height);
		}
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
			touchRegions.put(text, new TouchRegion(x, y, (int) fontBounds.width, (int) fontBounds.height, true));
		}
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		int width = Gdx.graphics.getWidth() / 2;
		int height = Gdx.graphics.getHeight() / 2;

		Integer touchX = null;
		Integer touchY = null;
		InputMultiplexer plex = (InputMultiplexer) Gdx.input.getInputProcessor();
		InGameInputProcessor ip = (InGameInputProcessor) plex.getProcessors().get(1);
		if (ip.didTouch()) {
			TouchPoint touchPoint = ip.getTouch();
			int x = touchPoint.x / 2;
			int y = touchPoint.y / 2;

			touchX = x;
			touchY = y;
			ip.consumeTouch();
		}

		viewMatrix.setToOrtho2D(0, 0, width, height);
		spriteBatch.setProjectionMatrix(viewMatrix);
		spriteBatch.setTransformMatrix(transformMatrix);
		spriteBatch.begin();

		String galcon = "GalCon";
		BitmapFont extraLargeFont = Fonts.getInstance().largeFont();
		int x = width / 2 - (int) extraLargeFont.getBounds(galcon).width / 2;
		extraLargeFont.draw(spriteBatch, galcon, x, (int) (height * .9f));

		BitmapFont smallFont = Fonts.getInstance().smallFont();
		if (hasUserInformation()) {

			createCoinDisplay(width, height);

			BitmapFont mediumFont = Fonts.getInstance().mediumFont();
			mediumFont.setColor(0.2f, 1.0f, 0.2f, 1.0f);
			smallFont.setColor(0.2f, 1.0f, 0.2f, 1.0f);
			x = width / 2 - (int) mediumFont.getBounds(currentUserText()).width / 2;
			mediumFont.draw(spriteBatch, currentUserText(), x, (int) (height * .8f));

			String toNextLevel = "To Next Level..." + (GameLoop.USER.rank.endAt - GameLoop.USER.xp + "xp");
			x = width / 2 - (int) smallFont.getBounds(toNextLevel).width / 2;
			smallFont.draw(spriteBatch, toNextLevel, x, (int) (height * .76f));
			smallFont.setColor(Color.WHITE);
			mediumFont.setColor(Color.WHITE);
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

			spriteBatch.begin();

			if (touchX != null) {
				for (Map.Entry<String, TouchRegion> touchRegionEntry : touchRegions.entrySet()) {
					TouchRegion touchRegion = touchRegionEntry.getValue();
					if (touchRegion.contains(touchX, touchY)) {
						String key = touchRegionEntry.getKey();

						if (key.equals(Constants.LEADERBOARDS)) {
							socialAction.showLeaderboards();
						} else {
							returnValue = key;
						}
					}
				}
			}

			updateFont();
		} else {
			String loadingUserInfo = "Loading User Information...";
			x = width / 2 - (int) smallFont.getBounds(loadingUserInfo).width / 2;
			smallFont.draw(spriteBatch, loadingUserInfo, x, (int) (height * .6f));
		}

		spriteBatch.end();
	}

	private void createCoinDisplay(int width, int height) {

		String coinsText = "";

		DateTime timeRemaining = GameLoop.USER.timeRemainingUntilCoinsAvailable();

		if (timeRemaining != null) {
			coinsText += timeRemaining.getMinuteOfHour() + ":" + timeRemaining.getSecondOfMinute();

		} else {
			coinsText += GameLoop.USER.coins;
		}

		BitmapFont extraLargeFont = Fonts.getInstance().mediumFont();
		double percentageOfWidth = width * 0.04;
		int x = (int) percentageOfWidth;
		extraLargeFont.draw(spriteBatch, coinsText, x, (int) (height * .97f));
	}

	private boolean hasUserInformation() {
		return GameLoop.USER.handle != null;
	}

	@Override
	public void resize(int width, int height) {
		touchRegions.clear();
		updateRankProgressBar(width, height);

		float tableHeight = height * 0.15f;
		googlePlusTable.clear();
		googlePlusTable.setX(0);
		googlePlusTable.setY(0);
		googlePlusTable.setWidth(width);
		googlePlusTable.setHeight(tableHeight);

		googlePlusTable.pad(5, 5, 5, 5);
		googlePlusTable.add(googlePlus).width(tableHeight - 10).height(tableHeight - 10);
		signInLabel = new Label("default", skin);

		String text = Strings.G_PLUS_SIGNED_OUT;
		if (socialAction.isLoggedInToGooglePlus()) {
			text = Strings.G_PLUS_SIGNED_IN;
		}
		signInLabel.setText(text);
		googlePlusTable.add(signInLabel).left().expandX().expandY().padLeft(width * 0.02f);

		googlePlusTable.layout();
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

		InputMultiplexer plex = new InputMultiplexer();
		plex.addProcessor(stage);
		plex.addProcessor(Gdx.input.getInputProcessor());

		Gdx.input.setInputProcessor(plex);
	}

	@Override
	public void hide() {
		InputMultiplexer plex = (InputMultiplexer) Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(plex.getProcessors().get(1));
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
		gameAction.findUserInformation(new SetPlayerResultHandler(GameLoop.USER), GameLoop.USER.name);
	}

	@Override
	public Object getRenderResult() {
		return returnValue;
	}

	@Override
	public void onSignInFailed() {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				signInLabel.setText(Strings.G_PLUS_SIGNED_OUT);
			}
		});
	}

	@Override
	public void onSignInSucceeded() {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				signInLabel.setText(Strings.G_PLUS_SIGNED_IN);
			}
		});
	}

	@Override
	public void onSignOut() {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				signInLabel.setText(Strings.G_PLUS_SIGNED_OUT);
			}
		});
	}
}

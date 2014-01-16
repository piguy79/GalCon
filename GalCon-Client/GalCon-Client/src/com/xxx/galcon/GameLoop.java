package com.xxx.galcon;

import org.joda.time.DateTime;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.InAppBillingAction;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.screen.Action;
import com.xxx.galcon.screen.BoardScreen;
import com.xxx.galcon.screen.MenuScreenContainer;
import com.xxx.galcon.screen.SetGameBoardResultHandler;

public class GameLoop extends Game {
	public static Player USER;
	public static Configuration CONFIG;

	private InGameInputProcessor inputProcessor = new InGameInputProcessor();

	private GL20 gl;
	public AssetManager assetManager = new AssetManager();
	public UISkin skin = new UISkin();
	public TweenManager tweenManager;

	private MenuScreenContainer menuScreenContainer;
	private BoardScreen boardScreen;

	private GameAction gameAction;
	private SocialAction socialAction;
	private InAppBillingAction inAppBillingAction;

	private boolean loadingNewCoins = false;

	public GameLoop(GameAction gameAction, SocialAction socialAction, InAppBillingAction inAppBillingAction) {
		this.gameAction = gameAction;
		this.socialAction = socialAction;
		this.inAppBillingAction = inAppBillingAction;

		UIConnectionWrapper.setGameAction(gameAction);
		ExternalActionWrapper.setActions(gameAction, inAppBillingAction);
		tweenManager = new TweenManager();

		Player player = new Player();
		GameLoop.USER = player;

		gameAction.setGameLoop(this);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}

	@Override
	public void create() {
		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
		GameLoop.USER.email = prefs.getString(Constants.EMAIL, "");

		/*
		 * Assume OpenGL ES 2.0 support has been validated by platform specific
		 * file.
		 */
		gl = Gdx.graphics.getGL20();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glEnable(GL20.GL_DEPTH_BUFFER_BIT);

		Gdx.input.setCatchBackKey(true);

		Gdx.input.setInputProcessor(inputProcessor);

		assetManager.load("data/images/gameBoard.atlas", TextureAtlas.class);
		assetManager.load("data/images/levels.atlas", TextureAtlas.class);
		assetManager.load("data/images/levelSelection.atlas", TextureAtlas.class);
		assetManager.load("data/images/menus.atlas", TextureAtlas.class);
		assetManager.load("data/images/planets.atlas", TextureAtlas.class);
		assetManager.load("data/images/social.atlas", TextureAtlas.class);

		assetManager.load("data/fonts/planet_numbers.png", Texture.class);

		assetManager.finishLoading();

		skin.initialize(assetManager);

		Tween.setCombinedAttributesLimit(4);

		boardScreen = new BoardScreen(skin, assetManager, tweenManager);
		menuScreenContainer = new MenuScreenContainer(skin, socialAction, gameAction, inAppBillingAction, assetManager,
				tweenManager);
		setScreen(menuScreenContainer);
	}

	public void reset() {
		menuScreenContainer.resetState();
		setScreen(menuScreenContainer);
	}

	@Override
	public void render() {
		super.render();
		checkCoinStats();

		tweenManager.update(Gdx.graphics.getDeltaTime());

		Object result = ((ScreenFeedback) getScreen()).getRenderResult();
		if (result != null) {
			if (getScreen() instanceof MenuScreenContainer) {
				if (result instanceof String) {
					String action = (String) result;
					if (action.startsWith(Action.PLAY)) {
						String level = action.split(":")[1];
						gameAction.matchPlayerToGame(new SetGameBoardResultHandler(boardScreen), GameLoop.USER.handle,
								Long.valueOf(level));
						openBoardScreen();
					} else if (action.startsWith(Action.PLAY_WITH_FRIENDS)) {
						String level = action.split(":")[1];
						gameAction.matchPlayerToGame(new SetGameBoardResultHandler(boardScreen), GameLoop.USER.handle,
								Long.valueOf(level));
						openBoardScreen();
					}
				} else if (result instanceof GameBoard) {
					boardScreen.resetState();
					boardScreen.setGameBoard((GameBoard) result);
					openBoardScreen();
				}
			} else if (getScreen() instanceof BoardScreen) {
				String action = (String) result;
				if (action.equals(Action.BACK)) {
					boardScreen.resetState();
					boardScreen.getPreviousScreen().resetState();
					setScreen(boardScreen.getPreviousScreen());
				}
			}
		}
	}

	private void openBoardScreen() {
		boardScreen.setPreviousScreen((MenuScreenContainer) getScreen());
		setScreen(boardScreen);
	}

	private void checkCoinStats() {
		if (GameLoop.USER != null && GameLoop.USER.coins != null && GameLoop.CONFIG != null) {
			DateTime timeRemaining = GameLoop.USER.timeRemainingUntilCoinsAvailable();

			if (timeRemaining != null) {
				loadingNewCoins = false;
			} else if (timeRemaining == null && GameLoop.USER.coins == 0 && GameLoop.USER.usedCoins != -1) {
				if (!loadingNewCoins) {
					loadingNewCoins = true;
					gameAction.addFreeCoins(new SetPlayerResultHandler(GameLoop.USER), GameLoop.USER.handle);
				}
			} else {
				loadingNewCoins = false;
			}
		}
	}
}

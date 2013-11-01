package com.xxx.galcon;

import org.joda.time.DateTime;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.http.SocialAction;
//github.com/piguy79/GalCon.git
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.screen.Action;
import com.xxx.galcon.screen.BoardScreen;
import com.xxx.galcon.screen.CurrentGameScreen;
import com.xxx.galcon.screen.GameListScreen;
import com.xxx.galcon.screen.LevelSelectionScreen;
import com.xxx.galcon.screen.MainMenuScreen;
import com.xxx.galcon.screen.NoMoreCoinsDialog;
import com.xxx.galcon.screen.SetGameBoardResultHandler;

public class GameLoop extends Game {
	public static Player USER;
	public static Configuration CONFIG;

	private InGameInputProcessor inputProcessor = new InGameInputProcessor();
	private BoardScreen boardScreen;
	private MainMenuScreen mainMenuScreen;
	private GameListScreen currentGameScreen;
	private LevelSelectionScreen levelSelectionScreen;
	private NoMoreCoinsDialog noMoreCoinsScreen;

	private GL20 gl;
	public AssetManager assetManager = new AssetManager();
	public UISkin skin = new UISkin();
	public TweenManager tweenManager;

	private GameAction gameAction;
	private SocialAction socialAction;
	

	private boolean loadingNewCoins = false;
	private boolean inAppBillingSetup = false;

	public GameLoop(Player player, GameAction gameAction, SocialAction socialAction, Configuration config) {
		this.gameAction = gameAction;
		this.socialAction = socialAction;
		GameLoop.CONFIG = config;
		GameLoop.USER = player;
		UIConnectionWrapper.setGameAction(gameAction);
		ExternalActionWrapper.setGameAction(gameAction);
		tweenManager = new TweenManager();
	}

	@Override
	public void pause() {
		Fonts.dispose();
		super.pause();
	}

	@Override
	public void resume() {
		Fonts.dispose();
		super.resume();
	}

	@Override
	public void create() {
		/*
		 * Assume OpenGL ES 2.0 support has been validated by platform specific
		 * file.
		 */
		gl = Gdx.graphics.getGL20();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glEnable(GL20.GL_DEPTH_BUFFER_BIT);

		Gdx.input.setCatchBackKey(true);

		Gdx.input.setInputProcessor(inputProcessor);

		TextureParameter param = new TextureParameter();
		param.minFilter = TextureFilter.Linear;
		param.magFilter = TextureFilter.Linear;

		assetManager.load("data/images/back.png", Texture.class, param);
		assetManager.load("data/images/rect_button_blank.png", Texture.class, param);
		assetManager.load("data/images/forward.png", Texture.class, param);
		assetManager.load("data/images/end_turn.png", Texture.class, param);
		assetManager.load("data/images/refresh.png", Texture.class, param);
		assetManager.load("data/fonts/planet_numbers.png", Texture.class, param);
		assetManager.load("data/images/transparent_square.png", Texture.class, param);
		assetManager.load("data/images/planets/planet3.png", Texture.class, param);
		assetManager.load("data/images/planets/planet3-touch.png", Texture.class, param);
		assetManager.load("data/images/bg1.png", Texture.class, param);
		assetManager.load("data/images/bg_dark_gray_10x10.png", Texture.class, param);
		assetManager.load("data/images/slash_line.png", Texture.class, param);
		assetManager.load("data/images/arrow_solid_line.png", Texture.class, param);
		assetManager.load("data/images/bottom_bar.png", Texture.class, param);
		assetManager.load("data/images/bottom_bar_expand_button.png", Texture.class, param);
		assetManager.load("data/images/bottom_bar_ship_button.png", Texture.class, param);
		assetManager.load("data/images/arrow_right_small_black.png", Texture.class, param);
		assetManager.load("data/images/arrow_left_small_black.png", Texture.class, param);
		assetManager.load("data/images/ok_button.png", Texture.class, param);
		assetManager.load("data/images/cancel_button.png", Texture.class, param);
		assetManager.load("data/images/ship_selection_dialog_bg.png", Texture.class, param);
		assetManager.load("data/images/ship.png", Texture.class, param);
		assetManager.load("data/images/level_selection_card.png", Texture.class, param);
		assetManager.load("data/images/levels/1.png", Texture.class, param);
		assetManager.load("data/images/levels/2.png", Texture.class, param);
		assetManager.load("data/images/levels/3.png", Texture.class, param);
		assetManager.load("data/images/social_play.png", Texture.class, param);
		assetManager.load("data/images/reg_play.png", Texture.class, param);
		assetManager.load("data/images/level_select_bg.png", Texture.class, param);
		assetManager.load("data/images/level_select_bg_bottom.png", Texture.class, param);
		assetManager.load("data/images/level_card_black.png", Texture.class, param);
		assetManager.load("data/images/level_select_card_shadow.png", Texture.class, param);
		assetManager.load("data/images/Google+_chiclet_Red.jpg", Texture.class, param);
		assetManager.load("data/images/green_button.png", Texture.class, param);
		assetManager.load("data/images/coins_bg.png", Texture.class, param);
		assetManager.load("data/images/black_grey_button.png", Texture.class, param);
		assetManager.load("data/images/planets/dead_planet.png", Texture.class, param);
		assetManager.load("data/images/planets/sun.png", Texture.class, param);

		assetManager.finishLoading();

		skin.initialize(assetManager);

		Tween.setCombinedAttributesLimit(4);

		boardScreen = new BoardScreen(assetManager, tweenManager);
		mainMenuScreen = new MainMenuScreen(this, skin, gameAction, socialAction);
		currentGameScreen = new CurrentGameScreen(assetManager);
		levelSelectionScreen = new LevelSelectionScreen(skin, assetManager);
		noMoreCoinsScreen = new NoMoreCoinsDialog(skin, assetManager);
		setScreen(mainMenuScreen);
	}

	@Override
	public void render() {
		super.render();
		checkCoindStats();
		setupInAppBilling();
		tweenManager.update(Gdx.graphics.getDeltaTime());

		ScreenFeedback screen = getScreen();
		Object result = screen.getRenderResult();
		if (result != null) {
			setScreen(nextScreen(screen, result));
		}
	}

	private ScreenFeedback nextScreen(ScreenFeedback currentScreen, Object result) {
		if (currentScreen instanceof MainMenuScreen) {
			String nextScreen = (String) result;

			if (nextScreen.equals(Constants.New)) {
				if (USER.coins == 0) {
					noMoreCoinsScreen.resetState();
					return noMoreCoinsScreen;
				}
				levelSelectionScreen.resetState();
				return levelSelectionScreen;
			} else if (nextScreen.equals(Constants.CONTINUE)) {
				currentGameScreen.resetState();
				UIConnectionWrapper.findCurrentGamesByPlayerHandle(currentGameScreen, USER.handle);
				return currentGameScreen;
			}
		} else if (currentScreen instanceof GameListScreen) {
			if (result instanceof GameBoard) {
				boardScreen.resetState();
				boardScreen.previousScreen = currentScreen;
				boardScreen.setGameBoard((GameBoard) result);
				return boardScreen;
			} else if (result instanceof String) {
				String action = (String) result;
				if (action.equals(Action.BACK)) {
					mainMenuScreen.resetState();
					return mainMenuScreen;
				}
			}
		} else if (currentScreen instanceof BoardScreen) {
			String action = (String) result;
			if (action.equals(Action.BACK)) {
				currentScreen.resetState();
				((BoardScreen) currentScreen).previousScreen.resetState();

				return ((BoardScreen) currentScreen).previousScreen;
			}
		} else if (currentScreen instanceof LevelSelectionScreen) {
			String action = (String) result;
			if (action.equals(Action.BACK)) {
				mainMenuScreen.resetState();
				return mainMenuScreen;
			} else if (action.startsWith(Action.PLAY)) {
				String level = action.split(":")[1];
				boardScreen.resetState();
				boardScreen.previousScreen = mainMenuScreen;
				gameAction.matchPlayerToGame(new SetGameBoardResultHandler(boardScreen), USER.handle,
						Long.valueOf(level));
				return boardScreen;
			} else if (action.startsWith(Action.PLAY_WITH_FRIENDS)) {
				String level = action.split(":")[1];
				boardScreen.resetState();
				boardScreen.previousScreen = mainMenuScreen;
				gameAction.matchPlayerToGame(new SetGameBoardResultHandler(boardScreen), USER.handle,
						Long.valueOf(level));
				return boardScreen;
			}
		} else if (currentScreen instanceof NoMoreCoinsDialog) {
			String action = (String) result;
			if (action.endsWith(Action.DIALOG_CANCEL)) {
				mainMenuScreen.resetState();
				return mainMenuScreen;
			}
		}

		throw new IllegalStateException("Cannot handle the result coming from screen: " + currentScreen);
	}

	private void checkCoindStats() {

		if (GameLoop.USER != null && GameLoop.USER.coins != null) {
			DateTime timeRemaining = GameLoop.USER.timeRemainingUntilCoinsAvailable();

			if (timeRemaining != null) {
				loadingNewCoins = false;

			} else if (timeRemaining == null && GameLoop.USER.coins == 0 && GameLoop.USER.usedCoins != -1) {
				if (!loadingNewCoins) {
					loadingNewCoins = true;
					try {
						gameAction.addCoins(new SetPlayerResultHandler(GameLoop.USER), GameLoop.USER.handle, 1,
								GameLoop.USER.usedCoins);
					} catch (ConnectionException e) {

					}
				}
			} else {
				loadingNewCoins = false;
			}

		}

	}
	
	private void setupInAppBilling(){
		if(GameLoop.USER != null && GameLoop.USER.handle != null && !inAppBillingSetup){
			gameAction.consumeExistingOrders();
			inAppBillingSetup = true;
		}
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public ScreenFeedback getScreen() {
		return (ScreenFeedback) super.getScreen();
	}
}

package com.xxx.galcon;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.screen.Action;
import com.xxx.galcon.screen.BoardScreen;
import com.xxx.galcon.screen.CurrentGameScreen;
import com.xxx.galcon.screen.GameListScreen;
import com.xxx.galcon.screen.JoinGameListScreen;
import com.xxx.galcon.screen.MainMenuScreen;
import com.xxx.galcon.screen.SetGameBoardResultHandler;

public class GameLoop extends Game {
	public static Player USER;
	private InGameInputProcessor inputProcessor = new InGameInputProcessor();
	private BoardScreen boardScreen;
	private MainMenuScreen mainMenuScreen;
	private GameListScreen currentGameScreen;
	private GameListScreen joinGameScreen;

	private GL20 gl;
	public AssetManager assetManager = new AssetManager();
	public TweenManager tweenManager;

	private GameAction gameAction;

	public GameLoop(Player player, GameAction gameAction) {
		this.gameAction = gameAction;
		GameLoop.USER = player;
		UIConnectionWrapper.setGameAction(gameAction);
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

		Gdx.input.setInputProcessor(inputProcessor);

		TextureParameter param = new TextureParameter();
		param.minFilter = TextureFilter.Linear;
		param.magFilter = TextureFilter.Linear;

		assetManager.load("data/images/arrow_left.png", Texture.class, param);
		assetManager.load("data/images/back.png", Texture.class, param);
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
		
		assetManager.finishLoading();
		
		Tween.setCombinedAttributesLimit(4);

		boardScreen = new BoardScreen(assetManager, tweenManager);
		mainMenuScreen = new MainMenuScreen(this, gameAction);
		currentGameScreen = new CurrentGameScreen(assetManager);
		joinGameScreen = new JoinGameListScreen(assetManager);
		setScreen(mainMenuScreen);
	}

	@Override
	public void render() {
		super.render();
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
			if (nextScreen.equals(Constants.CREATE)) {
				boardScreen.resetState();
				boardScreen.previousScreen = mainMenuScreen;
				int width = 5 + (int) (Math.random() * 4.0f);
				int height = (int) Math.ceil(width * 1.33f);
				int gameTypeToUse = (int) (Math.random() * Constants.gameTypes.size());
				gameAction.generateGame(new SetGameBoardResultHandler(boardScreen), USER.handle, width, height,
						Constants.gameTypes.get(gameTypeToUse));
				return boardScreen;
			} else if (nextScreen.equals(Constants.JOIN)) {
				joinGameScreen.resetState();
				return joinGameScreen;
			} else if (nextScreen.equals(Constants.CURRENT)) {
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
		}

		throw new IllegalStateException("Cannot handle the result coming from screen: " + currentScreen);
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

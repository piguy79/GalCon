package com.xxx.galcon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.screen.Action;
import com.xxx.galcon.screen.BoardScreen;
import com.xxx.galcon.screen.GameListScreen;
import com.xxx.galcon.screen.JoinGameListScreen;
import com.xxx.galcon.screen.MainMenuScreen;
import com.xxx.galcon.screen.SetGameBoardResultHandler;

public class GameLoop extends Game {
	public static Player USER;
	private InGameInputProcessor inputProcessor = new InGameInputProcessor();
	private BoardScreen boardScreen;
	private MainMenuScreen mainMenuScreen;
	private GL20 gl;
	public AssetManager assetManager = new AssetManager();

	private GameAction gameAction;

	public GameLoop(Player player, GameAction gameAction) {
		this.gameAction = gameAction;
		GameLoop.USER = player;
		UIConnectionWrapper.setGameAction(gameAction);

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

		assetManager.load("data/images/arrow_right.png", Texture.class, param);
		assetManager.load("data/images/arrow_left.png", Texture.class, param);
		assetManager.load("data/images/end_turn.png", Texture.class, param);
		assetManager.load("data/images/refresh.png", Texture.class, param);
		assetManager.load("data/images/ship_selection_dialog.png", Texture.class, param);
		assetManager.load("data/fonts/planet_numbers.png", Texture.class, param);
		assetManager.load("data/images/transparent_square.png", Texture.class, param);
		assetManager.finishLoading();

		boardScreen = new BoardScreen(assetManager);
		mainMenuScreen = new MainMenuScreen(this, gameAction);
		setScreen(mainMenuScreen);
	}

	@Override
	public void render() {
		super.render();

		ScreenFeedback screen = getScreen();
		Object result = screen.getRenderResult();
		if (result != null) {
			setScreen(nextScreen(screen, result));
		}
	}

	private ScreenFeedback nextScreen(ScreenFeedback currentScreen, Object result) {
		try {
			if (currentScreen instanceof MainMenuScreen) {
				String nextScreen = (String) result;
				if (nextScreen.equals(Constants.CREATE)) {
					boardScreen.resetState();
					gameAction.generateGame(new SetGameBoardResultHandler(boardScreen), USER.name, 6, 8);
					return boardScreen;
				} else if (nextScreen.equals(Constants.JOIN)) {
					GameListScreen joinScreen = new JoinGameListScreen(assetManager);
					UIConnectionWrapper.findAvailableGames(joinScreen, USER.name);
					return joinScreen;
				} else if (nextScreen.equals(Constants.CURRENT)) {
					GameListScreen currentGameScreen = new GameListScreen(assetManager);
					UIConnectionWrapper.findActiveGamesForAUser(currentGameScreen, USER.name);
					return currentGameScreen;
				}
			} else if (currentScreen instanceof GameListScreen) {
				if (result instanceof GameBoard) {
					boardScreen.resetState();
					GameBoard toTakeActionOn = (GameBoard) result;
					((GameListScreen) currentScreen).takeActionOnGameboard(gameAction, toTakeActionOn, USER.name,
							boardScreen);
					return boardScreen;
				} else if (result instanceof Action) {
					Action action = (Action) result;
					if (action == Action.BACK) {
						mainMenuScreen.resetState();
						return mainMenuScreen;
					}
				}
			} else if (currentScreen instanceof BoardScreen) {
				Action action = (Action) result;
				if (action == Action.BACK) {
					mainMenuScreen.resetState();
					return mainMenuScreen;
				}
			}
		} catch (ConnectionException e) {
			System.out.println(e);
			// FIXME: turn this into an error the user can see
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

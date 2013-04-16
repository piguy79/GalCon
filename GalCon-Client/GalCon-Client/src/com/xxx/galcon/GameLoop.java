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
import com.xxx.galcon.screen.BoardScreen;
import com.xxx.galcon.screen.BoardScreen.ReturnCode;
import com.xxx.galcon.screen.GameListScreen;
import com.xxx.galcon.screen.JoinGameListScreen;
import com.xxx.galcon.screen.MainMenuScreen;
import com.xxx.galcon.screen.SetGameBoardResultHandler;

public class GameLoop extends Game {
	public static String USER;
	private InGameInputProcessor inputProcessor = new InGameInputProcessor();
	private BoardScreen boardScreen;
	private MainMenuScreen mainMenuScreen;
	private GL20 gl;
	private AssetManager assetManager = new AssetManager();

	private GameAction gameAction;

	public GameLoop(String user, GameAction gameAction) {
		this.gameAction = gameAction;
		GameLoop.USER = user;
		ConnectionWrapper.setGameAction(gameAction);
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
		assetManager.finishLoading();

		boardScreen = new BoardScreen(assetManager);
		mainMenuScreen = new MainMenuScreen();
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
					gameAction.generateGame(new SetGameBoardResultHandler(boardScreen), USER, 7, 10);
					return boardScreen;
				} else if (nextScreen.equals(Constants.JOIN)) {
					GameListScreen joinScreen = new JoinGameListScreen();
					gameAction.findAvailableGames(joinScreen, USER);
					return joinScreen;
				} else if (nextScreen.equals(Constants.CURRENT_GAMES)) {
					GameListScreen currentGameScreen = new GameListScreen();
					gameAction.findActiveGamesForAUser(currentGameScreen, USER);
					return currentGameScreen;
				}
			} else if (currentScreen instanceof GameListScreen) {
				boardScreen.resetState();
				GameBoard toTakeActionOn = (GameBoard) result;
				((GameListScreen) currentScreen).takeActionOnGameboard(gameAction, toTakeActionOn, USER, boardScreen);
				return boardScreen;
			} else if (currentScreen instanceof BoardScreen) {
				ReturnCode returnCode = (ReturnCode) result;
				if (returnCode == ReturnCode.BACK) {
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

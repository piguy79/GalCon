package com.xxx.galcon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.screen.BoardScreen;
import com.xxx.galcon.screen.JoinScreen;
import com.xxx.galcon.screen.MainMenuScreen;

public class GameLoop extends Game {
	// FIXME: this needs to be replaced by a unique user id
	public static final String USER = "me";
	private InGameInputProcessor inputProcessor = new InGameInputProcessor();
	private BoardScreen boardScreen;
	private MainMenuScreen mainMenuScreen;
	private GL20 gl;

	private GameAction gameAction;

	public GameLoop(GameAction gameAction) {
		this.gameAction = gameAction;
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
		gl.glEnable(GL20.GL_DEPTH_TEST);

		Gdx.input.setInputProcessor(inputProcessor);

		boardScreen = new BoardScreen();

		setScreen(new MainMenuScreen());
	}

	@Override
	public void render() {
		super.render();

		ScreenFeedback screen = getScreen();
		Object result = screen.getRenderResult();
		if (result != null) {
			screen.dispose();

			setScreen(nextScreen(screen, result));
		}
	}

	private ScreenFeedback nextScreen(ScreenFeedback currentScreen, Object result) {
		try {
			if (currentScreen instanceof MainMenuScreen) {
				String nextScreen = (String) result;
				if (nextScreen.equals("Create")) {
					boardScreen.setGameBoard(gameAction.generateGame(USER, 12, 16));
					return boardScreen;
				} else if (nextScreen.equals("Join")) {
					return new JoinScreen(gameAction.findAllGames());
				}
			} else if (currentScreen instanceof JoinScreen) {
				GameBoard gameToJoin = (GameBoard) result;
				boardScreen.setGameBoard(gameAction.joinGame(gameToJoin.id, "NewPlayer"));
				setScreen(boardScreen);
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

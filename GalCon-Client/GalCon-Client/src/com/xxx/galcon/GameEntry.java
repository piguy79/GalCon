package com.xxx.galcon;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.model.GameBoard;

public class GameEntry implements ApplicationListener {
	private InGameInputProcessor inputProcessor = new InGameInputProcessor();
	private Camera camera;
	private GameLoop gameLoop;
	private MainMenu mainMenu;
	private GL20 gl;
	private Screen currentScreen = null;

	private GameAction gameAction;

	public GameEntry(GameAction gameAction) {
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

		gameLoop = new GameLoop();
	}

	@Override
	public void dispose() {

	}

	@Override
	public void render() {
		if (currentScreen == null) {
			currentScreen = new MainMenu();
		}

		boolean keepRunning = currentScreen.render(gl, camera);

		if (!keepRunning) {
			currentScreen.dispose();

			if (currentScreen instanceof MainMenu) {
				String nextScreen = (String) currentScreen.getRenderReturnValue();
				if (nextScreen.equals("create")) {
					gameLoop.newGame(gameAction, gl);
					currentScreen = gameLoop;
				} else if (nextScreen.equals("join")) {
					currentScreen = new JoinScreen(gameAction);
				}
			} else if (currentScreen instanceof JoinScreen) {
				GameBoard gameToJoin = (GameBoard) currentScreen.getRenderReturnValue();
				gameLoop.joinGame(gameAction, gameToJoin, gl);
				currentScreen = gameLoop;
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		camera = new PerspectiveCamera(67f, width, height);
		camera.near = 1.0f;
		camera.far = 5000f;
		camera.translate(0.0f, 0.0f, 10.0f);
		camera.lookAt(0.0f, 0.0f, 0.0f);

		camera.update();

		gl.glViewport(0, 0, width, height);

		gameLoop.resize(gl, camera);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}

package com.xxx.galcon.screen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.ConnectionResultCallback;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;

public class GameListScreen implements ScreenFeedback, ConnectionResultCallback<AvailableGames> {
	private BitmapFont smallFont;
	private BitmapFont mediumFont;
	private SpriteBatch spriteBatch;
	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();
	private GameBoard returnValue;
	private AvailableGames allGames;

	public GameListScreen() {
		spriteBatch = new SpriteBatch();
		smallFont = Fonts.getInstance().smallFont();
		mediumFont = Fonts.getInstance().mediumFont();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		float width = Gdx.graphics.getWidth() / 2;
		float height = Gdx.graphics.getHeight() / 2;

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
		spriteBatch.enableBlending();

		if (allGames == null) {
			String text = "Loading...";
			float halfFontWidth = mediumFont.getBounds(text).width / 2;
			mediumFont.draw(spriteBatch, text, width / 2 - halfFontWidth, height * .4f);
		} else if (allGames.getAllGames().isEmpty()) {
			String text = "No games available";
			float halfFontWidth = mediumFont.getBounds(text).width / 2;
			mediumFont.draw(spriteBatch, text, width / 2 - halfFontWidth, height * .4f);
		} else {
			float textY = 0.98f;
			for (GameBoard gameBoard : allGames.getAllGames()) {
				String text = createLabelTestForAGame(gameBoard);
				float halfFontWidth = smallFont.getBounds(text).width / 2;
				smallFont.draw(spriteBatch, text, width / 2 - halfFontWidth, height * textY);
				if (touchX != null && touchX >= width / 2 - halfFontWidth && touchX <= width / 2 + halfFontWidth) {
					if (touchY != null && touchY <= height * textY && touchY >= height * (textY - .03f)) {
						returnValue = gameBoard;
					}
				}

				textY -= 0.05f;
			}
		}

		spriteBatch.end();
	}

	private String createLabelTestForAGame(GameBoard gameBoard) {
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		String labelForGame = format.format(gameBoard.createdDate);
		List<String> otherPlayers = gameBoard.allPlayersExcept(GameLoop.USER);
		if (otherPlayers.size() == 0) {
			return labelForGame + " waiting for opponent";

		}

		return labelForGame + " vs " + gameBoard.allPlayersExcept(GameLoop.USER);
	}

	public BoardScreen takeActionOnGameboard(GameAction gameAction, GameBoard toTakeActionOn, String user,
			BoardScreen boardScreen) {
		try {
			gameAction.findGameById(new SetGameBoardResultHandler(boardScreen), toTakeActionOn.id);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
		return boardScreen;
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

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
	public Object getRenderResult() {
		return returnValue;
	}

	@Override
	public void resetState() {
		returnValue = null;
	}

	@Override
	public void result(AvailableGames result) {
		this.allGames = result;
	}
}

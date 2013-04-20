package com.xxx.galcon.screen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
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
import com.xxx.galcon.screen.hud.GameListHud;
import com.xxx.galcon.screen.hud.Hud;

public class GameListScreen implements ScreenFeedback, ConnectionResultCallback<AvailableGames> {
	private BitmapFont smallFont;
	private BitmapFont mediumFont;
	private SpriteBatch spriteBatch;
	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();
	private Object returnValue;
	private AvailableGames allGames;
	private Hud gameListHud;

	public GameListScreen(AssetManager assetManager) {
		spriteBatch = new SpriteBatch();
		smallFont = Fonts.getInstance().smallFont();
		mediumFont = Fonts.getInstance().mediumFont();
		gameListHud = new GameListHud(assetManager);
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
	}

	protected boolean showGamesThatHaveBeenWon() {
		return true;
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
		} else {
			List<GameBoard> games = new ArrayList<GameBoard>(allGames.getAllGames());
			for (ListIterator<GameBoard> iter = games.listIterator(); iter.hasNext();) {
				GameBoard board = iter.next();
				if (board.winner != null && !board.winner.isEmpty()) {
					if (!showGamesThatHaveBeenWon()
							|| board.createdDate.before(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24))) {
						iter.remove();
					}
				}
			}

			if (games.isEmpty()) {
				String text = "No games available";
				float halfFontWidth = mediumFont.getBounds(text).width / 2;
				mediumFont.draw(spriteBatch, text, width / 2 - halfFontWidth, height * .4f);
			} else {
				float textY = 0.98f;
				for (GameBoard gameBoard : games) {
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
		}

		spriteBatch.end();

		gameListHud.render(delta);

		if (gameListHud.getRenderResult() != null) {
			returnValue = gameListHud.getRenderResult();
		}
	}

	private String createLabelTestForAGame(GameBoard gameBoard) {
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		String labelForGame = format.format(gameBoard.createdDate);

		if (gameBoard.winner != null && !gameBoard.winner.isEmpty()) {
			String winningText = "You Lost";
			if (gameBoard.winner.equals(GameLoop.USER)) {
				winningText = "You Won!!";
			}
			return labelForGame + " " + winningText;
		} else {

			List<String> otherPlayers = gameBoard.allPlayersExcept(GameLoop.USER);
			if (otherPlayers.size() == 0) {
				return labelForGame + " waiting for opponent";

			}

			return labelForGame + " vs " + gameBoard.allPlayersExcept(GameLoop.USER);
		}
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

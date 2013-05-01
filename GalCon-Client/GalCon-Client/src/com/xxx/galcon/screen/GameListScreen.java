package com.xxx.galcon.screen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.xxx.galcon.Fonts;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.ScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.screen.hud.GameListHud;
import com.xxx.galcon.screen.hud.Hud;

public class GameListScreen implements ScreenFeedback, UIConnectionResultCallback<AvailableGames> {
	private BitmapFont smallFont;
	private BitmapFont mediumFont;
	private SpriteBatch spriteBatch;
	private final Matrix4 viewMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();
	private Object returnValue;
	private AvailableGames allGames;
	private Hud gameListHud;
	private String loadingMessage = "Loading...";

	public GameListScreen(AssetManager assetManager) {
		spriteBatch = new SpriteBatch();
		gameListHud = new GameListHud(assetManager);

		resume();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
	}

	protected boolean showGamesThatHaveBeenWon() {
		return true;
	}

	protected void refreshScreen() {
		UIConnectionWrapper.findActiveGamesForAUser(this, GameLoop.USER.name);
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
			float halfFontWidth = mediumFont.getBounds(loadingMessage).width / 2;
			mediumFont.draw(spriteBatch, loadingMessage, width / 2 - halfFontWidth, height * .4f);
		} else {
			List<GameBoard> games = new ArrayList<GameBoard>(allGames.getAllGames());
			for (ListIterator<GameBoard> iter = games.listIterator(); iter.hasNext();) {
				GameBoard board = iter.next();
				if (board.hasWinner()) {
					if (!showGamesThatHaveBeenWon()
							|| board.endGameInformation.winningDate.before(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24))) {
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
					String text = createLabelTextForAGame(gameBoard);
					float halfFontWidth = smallFont.getBounds(text).width / 2;

					if (GameLoop.USER.name.equals(gameBoard.currentPlayerToMove) && !gameBoard.hasWinner()) {
						smallFont.setColor(0.2f, 1.0f, 0.2f, 1.0f);
					}
					smallFont.draw(spriteBatch, text, width / 2 - halfFontWidth, height * textY);
					smallFont.setColor(Color.WHITE);
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
			Action result = (Action) gameListHud.getRenderResult();
			if (result == Action.REFRESH) {
				refreshScreen();
			} else {
				returnValue = result;
			}
		}
	}

	private String createLabelTextForAGame(GameBoard gameBoard) {
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		String labelForGame = format.format(gameBoard.createdDate);

		if (gameBoard.hasWinner()) {
			String winningText = "You Lost";
			if (gameBoard.endGameInformation.winner.equals(GameLoop.USER.name)) {
				winningText = "You Won!!";
			}
			return labelForGame + " " + winningText;
		} else {

			List<String> otherPlayers = gameBoard.allPlayersExcept(GameLoop.USER.name);
			if (otherPlayers.size() == 0) {
				return labelForGame + " waiting for opponent";
			}

			return labelForGame + " vs " + gameBoard.allPlayersExcept(GameLoop.USER.name);
		}
	}

	public BoardScreen takeActionOnGameboard(GameAction gameAction, GameBoard toTakeActionOn, String user,
			BoardScreen boardScreen) {
		UIConnectionWrapper.findGameById(new SetGameBoardResultHandler(boardScreen), toTakeActionOn.id);
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
		smallFont = Fonts.getInstance().smallFont();
		mediumFont = Fonts.getInstance().mediumFont();
	}

	@Override
	public Object getRenderResult() {
		return returnValue;
	}

	@Override
	public void resetState() {
		returnValue = null;
		loadingMessage = "Loading...";
	}

	@Override
	public void onConnectionResult(AvailableGames result) {
		this.allGames = result;
	}

	@Override
	public void onConnectionError(String msg) {
		loadingMessage = "Unable to connect at the moment";
	}
}

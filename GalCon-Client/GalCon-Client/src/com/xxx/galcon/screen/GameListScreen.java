package com.xxx.galcon.screen;

import static com.badlogic.gdx.math.Interpolation.pow3;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static com.xxx.galcon.Constants.CONNECTION_ERROR_MESSAGE;
import static com.xxx.galcon.Util.createShader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ExternalActionWrapper;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.MinifiedGame;
import com.xxx.galcon.screen.hud.HeaderHud;
import com.xxx.galcon.screen.overlay.DismissableOverlay;
import com.xxx.galcon.screen.overlay.Overlay;
import com.xxx.galcon.screen.overlay.TextOverlay;
import com.xxx.galcon.screen.widget.ShaderLabel;

public class GameListScreen implements PartialScreenFeedback, UIConnectionResultCallback<AvailableGames> {
	private Object returnValue;
	private AvailableGames allGames;
	private String loadingMessage = "Loading...";
	protected Overlay overlay;
	protected AssetManager assetManager;

	private ShaderProgram fontShader;

	private UISkin skin;

	private Stage stage;
	private ShaderLabel loadingTextLabel;
	private Table gameListTable;
	private Table gamesTable;
	private ImageButton backButton;

	private TextureAtlas menusAtlas;

	public GameListScreen(AssetManager assetManager, UISkin skin) {
		this.assetManager = assetManager;
		this.skin = skin;

		menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);

		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
	}

	protected boolean showGamesThatHaveBeenWon() {
		return true;
	}

	protected void refreshScreen() {
		overlay = new TextOverlay("Refreshing...", menusAtlas, true, assetManager);
		UIConnectionWrapper.findCurrentGamesByPlayerHandle(this, GameLoop.USER.handle);
	}

	@Override
	public void render(float delta) {

	}

	private String createLabelTextForAGame(MinifiedGame gameBoard) {
		DateFormat format = new SimpleDateFormat("MM/dd HH:mm");
		String labelForGame = format.format(gameBoard.createdDate);

		if (gameBoard.hasWinner()) {
			String winningText = "You Lost";
			if (gameBoard.winner.equals(GameLoop.USER.handle)) {
				winningText = "You Won!!";
			}
			return labelForGame + " " + winningText;
		} else {

			List<String> otherPlayers = gameBoard.allPlayersExcept(GameLoop.USER.handle);
			if (otherPlayers.size() == 0) {
				return labelForGame + " waiting for opponent";
			}

			return labelForGame + " vs " + playerInfoText(otherPlayers);
		}
	}

	private String playerInfoText(List<String> otherPlayers) {

		String playerDescription = "";
		for (String player : otherPlayers) {
			// playerDescription = playerDescription + " [" + player + " (Lvl "
			// + player.rank.level + ") ]";
			playerDescription = playerDescription + " [" + player + "]";
		}
		return playerDescription;
	}

	public void takeActionOnGameboard(MinifiedGame toTakeActionOn, String playerHandle) {
		UIConnectionWrapper.findGameById(new SelectGameResultHander(), toTakeActionOn.id, GameLoop.USER.handle);
	}

	@Override
	public void hide() {
		this.stage = null;
	}

	@Override
	public Object getRenderResult() {
		return returnValue;
	}

	@Override
	public void resetState() {
		returnValue = null;
		allGames = null;
		overlay = null;
		loadingMessage = "Loading...";
	}

	@Override
	public void onConnectionResult(AvailableGames result) {
		returnValue = null;
		allGames = null;
		overlay = null;
		allGames = result;

		showGames();
	}

	private void showGames() {
		if (allGames == null || stage == null) {
			return;
		}

		List<MinifiedGame> games = new ArrayList<MinifiedGame>(allGames.getAllGames());
		for (ListIterator<MinifiedGame> iter = games.listIterator(); iter.hasNext();) {
			MinifiedGame board = iter.next();
			if (board.hasWinner()) {
				if (!showGamesThatHaveBeenWon()
						|| board.winningDate.before(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24))) {
					iter.remove();
				}
			}
		}

		if (games.isEmpty()) {
			loadingMessage = "No games available";
		} else {
			for (MinifiedGame gameBoard : games) {
				String text = createLabelTextForAGame(gameBoard);
				ShaderLabel label = new ShaderLabel(fontShader, text, skin, Constants.UI.DEFAULT_FONT);
				gameListTable.add(label);

				gameListTable.row();
			}
		}
	}

	@Override
	public void onConnectionError(String msg) {
		returnValue = null;
		allGames = null;
		overlay = null;
		loadingMessage = Constants.CONNECTION_ERROR_MESSAGE;
	}

	protected class SelectGameResultHander implements UIConnectionResultCallback<GameBoard> {

		@Override
		public void onConnectionResult(GameBoard result) {
			returnValue = result;
			overlay = null;
		}

		@Override
		public void onConnectionError(String msg) {
			overlay = new DismissableOverlay(menusAtlas, new TextOverlay(CONNECTION_ERROR_MESSAGE, "medium",
					menusAtlas, assetManager));
		}
	}

	private void startHideSequence(final String retVal) {
		int modifier = -1;
		if (retVal.equals(Action.BACK)) {
			modifier = 1;
		}

		backButton.addAction(sequence(delay(0.25f),
				moveTo(modifier * Gdx.graphics.getWidth(), backButton.getY(), 0.9f, pow3), run(new Runnable() {
					@Override
					public void run() {
						backButton.remove();
						returnValue = retVal;
					}
				})));
	}

	@Override
	public void show(Stage stage, float width, float height) {
		ExternalActionWrapper.recoverUsedCoinsCount();

		int buttonHeight = (int) (Gdx.graphics.getHeight() * (HeaderHud.HEADER_HEIGHT_RATIO * 0.88f));
		backButton = new ImageButton(skin, "backButton");
		backButton.setX(10);
		backButton.setY(height - buttonHeight - 5);
		backButton.setWidth(buttonHeight);
		backButton.setHeight(buttonHeight);
		backButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				startHideSequence(Action.BACK);
			}
		});

		loadingTextLabel = new ShaderLabel(fontShader, loadingMessage, skin, Constants.UI.DEFAULT_FONT);
		loadingTextLabel.setAlignment(Align.center);
		loadingTextLabel.setWidth(width);
		loadingTextLabel.setX(width / 2 - loadingTextLabel.getWidth() / 2);
		loadingTextLabel.setY(0.45f * height);
		loadingTextLabel.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

			}
		});
		stage.addActor(loadingTextLabel);

		gamesTable = new Table();
		final ScrollPane scrollPane = new ScrollPane(gamesTable);
		scrollPane.setScrollingDisabled(true, false);
		scrollPane.setFadeScrollBars(false);

		float tableHeight = height * 0.8f;
		gameListTable = new Table();
		gameListTable.setX(0);
		gameListTable.setY(height * .5f - tableHeight * .42f);
		gameListTable.setWidth(width);
		gameListTable.setHeight(tableHeight);
		gameListTable.add(scrollPane);
		gameListTable.top();
		stage.addActor(gameListTable);

		this.stage = stage;
		showGames();
	}

	@Override
	public boolean hideTitleArea() {
		return true;
	}
}

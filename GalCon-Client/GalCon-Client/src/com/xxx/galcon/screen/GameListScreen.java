package com.xxx.galcon.screen;

import static com.xxx.galcon.Constants.CONNECTION_ERROR_MESSAGE;
import static com.xxx.galcon.Util.createShader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ExternalActionWrapper;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.Strings;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Map;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.MinifiedGame;
import com.xxx.galcon.model.MinifiedGame.MinifiedPlayer;
import com.xxx.galcon.screen.hud.HeaderHud;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class GameListScreen implements PartialScreenFeedback, UIConnectionResultCallback<AvailableGames> {
	private Object returnValue;
	private AvailableGames allGames;
	private Maps allMaps;

	protected AssetManager assetManager;

	private ShaderProgram fontShader;

	private UISkin skin;

	private Stage stage;
	private ShaderLabel messageLabel;
	private Table gameListTable;
	private Table gamesTable;
	private ImageButton backButton;
	private Array<Actor> actors = new Array<Actor>();
	protected WaitImageButton waitImage;

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
		waitImage.start();
		UIConnectionWrapper.findCurrentGamesByPlayerHandle(this, GameLoop.USER.handle);
	}

	@Override
	public void render(float delta) {

	}

	private String playerInfoText(List<MinifiedPlayer> otherPlayers) {

		String playerDescription = "";
		for (MinifiedPlayer player : otherPlayers) {
			playerDescription = player.handle + " (Level " + player.rank + ")";
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
	}

	@Override
	public void onConnectionResult(AvailableGames result) {
		returnValue = null;
		allGames = null;
		allGames = result;

		waitImage.stop();

		showGames();
	}

	private void showGames() {
		if (allGames == null) {
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
			messageLabel.setText("No games available");
		} else {
			float height = Gdx.graphics.getHeight();
			float width = Gdx.graphics.getWidth();
			float rowHeight = height * 0.15f;
			for (MinifiedGame game : games) {
				gamesTable.add(createGameEntry(game)).height(rowHeight).width(width);
				gamesTable.row();
			}
		}
	}

	private Group createGameEntry(final MinifiedGame game) {
		float height = Gdx.graphics.getHeight();
		float width = Gdx.graphics.getWidth();

		float rowHeight = height * 0.15f;

		Image imageRow = new Image(skin, Constants.UI.CELL_BG);
		imageRow.setHeight(rowHeight);
		imageRow.setWidth(width);
		imageRow.setAlign(Align.center);

		Group group = new Group();
		group.addActor(imageRow);
		group.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				takeActionOnGameboard(game, GameLoop.USER.handle);
			}
		});

		String opponent;
		List<MinifiedPlayer> otherPlayers = game.allPlayersExcept(GameLoop.USER.handle);
		if (otherPlayers.size() == 0) {
			opponent = "[waiting for opponent]";
		} else {
			opponent = "vs " + playerInfoText(otherPlayers);
		}

		ShaderLabel vsLabel = new ShaderLabel(fontShader, opponent, skin, Constants.UI.DEFAULT_FONT);
		vsLabel.setAlignment(Align.center);
		vsLabel.setWidth(width);
		vsLabel.setY(rowHeight * 0.6f);
		group.addActor(vsLabel);

		String mapTitle = "";
		if (allMaps != null) {
			for (Map map : allMaps.allMaps) {
				if (map.key == game.mapKey) {
					mapTitle = "Map: " + map.title;
					break;
				}
			}
		}
		ShaderLabel mapLabel = new ShaderLabel(fontShader, mapTitle, skin, Constants.UI.SMALL_FONT);
		mapLabel.setAlignment(Align.left);
		mapLabel.setWidth(width);
		mapLabel.setY(rowHeight * 0.15f);
		mapLabel.setX(width * 0.08f);

		group.addActor(mapLabel);

		return group;
	}

	@Override
	public void onConnectionError(String msg) {
		returnValue = null;
		allGames = null;

		waitImage.stop();
		messageLabel.setText(CONNECTION_ERROR_MESSAGE);
	}

	protected class SelectGameResultHander implements UIConnectionResultCallback<GameBoard> {

		@Override
		public void onConnectionResult(GameBoard result) {
			returnValue = result;
		}

		@Override
		public void onConnectionError(String msg) {
			messageLabel.setText(CONNECTION_ERROR_MESSAGE);
		}
	}

	private void startHideSequence(final String retVal) {
		GraphicsUtils.hideAnimated(actors, retVal.equals(Action.BACK), new Runnable() {
			@Override
			public void run() {
				returnValue = retVal;
			}
		});
	}

	@Override
	public void show(Stage stage, final float width, float height) {
		ExternalActionWrapper.recoverUsedCoinsCount();
		actors.clear();

		waitImage = new WaitImageButton(skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

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
		actors.add(backButton);
		stage.addActor(backButton);

		messageLabel = new ShaderLabel(fontShader, "", skin, Constants.UI.DEFAULT_FONT);
		messageLabel.setAlignment(Align.center);
		messageLabel.setWidth(width);
		messageLabel.setX(width / 2 - messageLabel.getWidth() / 2);
		messageLabel.setY(0.45f * height);
		messageLabel.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

			}
		});
		actors.add(messageLabel);
		stage.addActor(messageLabel);

		final float tableHeight = height * 0.89f;
		gamesTable = new Table();
		gamesTable.top();
		final ScrollPane scrollPane = new ScrollPane(gamesTable) {
			public float getPrefHeight() {
				return tableHeight;
			}

			@Override
			public float getPrefWidth() {
				return width;
			}
		};
		scrollPane.setScrollingDisabled(true, false);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setX(0);
		scrollPane.setY(0);
		scrollPane.setWidth(width);
		scrollPane.setHeight(tableHeight);

		actors.add(scrollPane);
		stage.addActor(scrollPane);

		this.stage = stage;

		waitImage.start();
		UIConnectionWrapper.findAllMaps(mapResultCallback);
	}

	@Override
	public boolean hideTitleArea() {
		return true;
	}

	private UIConnectionResultCallback<Maps> mapResultCallback = new UIConnectionResultCallback<Maps>() {

		@Override
		public void onConnectionResult(Maps result) {
			allMaps = result;
			UIConnectionWrapper.findCurrentGamesByPlayerHandle(GameListScreen.this, GameLoop.USER.handle);
		}

		@Override
		public void onConnectionError(String msg) {
			GameListScreen.this.onConnectionError(msg);
		}
	};
}

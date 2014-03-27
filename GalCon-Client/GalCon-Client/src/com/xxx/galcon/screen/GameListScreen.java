package com.xxx.galcon.screen;

import static com.xxx.galcon.Constants.CONNECTION_ERROR_MESSAGE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Map;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.MinifiedGame;
import com.xxx.galcon.model.MinifiedGame.MinifiedPlayer;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.screen.overlay.LoadingOverlay;
import com.xxx.galcon.screen.widget.ScrollList;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class GameListScreen implements PartialScreenFeedback, UIConnectionResultCallback<AvailableGames> {
	private Object returnValue;
	private AvailableGames allGames;
	private Maps allMaps;

	private Resources resources;

	private Stage stage;
	private ShaderLabel messageLabel;
	private ScrollList<MinifiedGame> scrollList;
	private ImageButton backButton;
	private Array<Actor> actors = new Array<Actor>();
	protected WaitImageButton waitImage;
	private LoadingOverlay loadingOverlay;

	public GameListScreen(Resources resources) {
		this.resources = resources;
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
		loadingOverlay = new LoadingOverlay(resources);
		stage.addActor(loadingOverlay);
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
					break;
				}
			}
		}

		if (games.isEmpty()) {
			messageLabel.setText("No games available");
		} else {
			Collections.sort(games, new Comparator<MinifiedGame>() {
				@Override
				public int compare(MinifiedGame o1, MinifiedGame o2) {
					if (o1.moveAvailable && o2.moveAvailable) {
						return 0;
					} else if (o1.moveAvailable && !o2.moveAvailable) {
						return -1;
					}
					return 1;
				}
			});

			for (final MinifiedGame game : games) {
				scrollList.addRow(game, new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						takeActionOnGameboard(game, GameLoop.USER.handle);
					}
				});
			}
		}
	}

	private void createGameEntry(MinifiedGame game, Group group) {
		float width = group.getWidth();
		float rowHeight = group.getHeight();

		String opponent;
		List<MinifiedPlayer> otherPlayers = game.allPlayersExcept(GameLoop.USER.handle);
		if (otherPlayers.size() == 0) {
			opponent = BoardScreen.Labels.waitingLabel(game.social);
		} else {
			opponent = "vs " + playerInfoText(otherPlayers);
		}

		ShaderLabel vsLabel = new ShaderLabel(resources.fontShader, opponent, resources.skin, Constants.UI.DEFAULT_FONT);
		vsLabel.setAlignment(Align.center);
		vsLabel.setWidth(width);
		vsLabel.setY(rowHeight * 0.6f);
		group.addActor(vsLabel);

		String statusText = "";
		String statusFont = Constants.UI.DEFAULT_FONT_GREEN;
		if (game.hasWinner()) {
			if (game.winner.equals(GameLoop.USER.handle)) {
				statusText = "You Won";
			} else {
				statusText = "You Lost";
				statusFont = Constants.UI.DEFAULT_FONT_RED;
			}
		} else if(game.hasBeenDeclined()){
			statusText = "-- Invite Declined --";
			statusFont = Constants.UI.DEFAULT_FONT_RED;
		}else if (game.moveAvailable) {
			statusText = "--your move--";
		}

		if (!statusText.isEmpty()) {
			ShaderLabel yourMoveLabel = new ShaderLabel(resources.fontShader, statusText, resources.skin, statusFont);
			yourMoveLabel.setAlignment(Align.center);
			yourMoveLabel.setWidth(width);
			yourMoveLabel.setY(rowHeight * 0.4f);
			group.addActor(yourMoveLabel);
		}

		String mapTitle = "";
		if (allMaps != null) {
			for (Map map : allMaps.allMaps) {
				if (map.key == game.mapKey) {
					mapTitle = "Map: " + map.title;
					break;
				}
			}
		}
		ShaderLabel mapLabel = new ShaderLabel(resources.fontShader, mapTitle, resources.skin,
				Constants.UI.X_SMALL_FONT);
		mapLabel.setAlignment(Align.left);
		mapLabel.setWidth(width);
		mapLabel.setY(rowHeight * 0.15f);
		mapLabel.setX(width * 0.08f);

		group.addActor(mapLabel);
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
			loadingOverlay.remove();
			startHideSequence(result);
		}

		@Override
		public void onConnectionError(String msg) {
			loadingOverlay.remove();
			messageLabel.setText(CONNECTION_ERROR_MESSAGE);
		}
	}

	private void startHideSequence(final Object retVal) {
		waitImage.stop();
		GraphicsUtils.hideAnimated(actors, retVal.equals(Action.BACK), new Runnable() {
			@Override
			public void run() {
				returnValue = retVal;
			}
		});
	}

	@Override
	public void show(Stage stage, final float width, float height) {
		actors.clear();

		waitImage = new WaitImageButton(resources.skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		backButton = new ImageButton(resources.skin, "backButton");
		GraphicsUtils.setCommonButtonSize(backButton);
		backButton.setX(10);
		backButton.setY(height - backButton.getHeight() - 5);
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

		messageLabel = new ShaderLabel(resources.fontShader, "", resources.skin, Constants.UI.DEFAULT_FONT);
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
		scrollList = new ScrollList<MinifiedGame>(resources.skin) {
			@Override
			public void buildCell(MinifiedGame item, Group group) {
				createGameEntry(item, group);
			}
		};
		scrollList.setX(0);
		scrollList.setY(0);
		scrollList.setWidth(width);
		scrollList.setHeight(tableHeight);

		actors.add(scrollList);
		stage.addActor(scrollList);

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

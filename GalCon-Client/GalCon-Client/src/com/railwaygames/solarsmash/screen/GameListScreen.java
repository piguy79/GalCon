package com.railwaygames.solarsmash.screen;

import static com.railwaygames.solarsmash.Constants.CONNECTION_ERROR_MESSAGE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.PartialScreenFeedback;
import com.railwaygames.solarsmash.UIConnectionWrapper;
import com.railwaygames.solarsmash.config.ConfigResolver;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.AvailableGames;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.Map;
import com.railwaygames.solarsmash.model.Maps;
import com.railwaygames.solarsmash.model.MinifiedGame;
import com.railwaygames.solarsmash.model.MinifiedGame.MinifiedPlayer;
import com.railwaygames.solarsmash.screen.overlay.DismissableOverlay;
import com.railwaygames.solarsmash.screen.overlay.LoadingOverlay;
import com.railwaygames.solarsmash.screen.overlay.Overlay;
import com.railwaygames.solarsmash.screen.overlay.TextOverlay;
import com.railwaygames.solarsmash.screen.widget.ScrollList;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;
import com.railwaygames.solarsmash.screen.widget.WaitImageButton;

public class GameListScreen implements PartialScreenFeedback, UIConnectionResultCallback<AvailableGames> {
	private Object returnValue;
	private AvailableGames allGames;
	private Maps allMaps;

	private Resources resources;

	private Stage stage;
	private ShaderLabel messageLabel;
	private ScrollList<MinifiedGame> scrollList;
	private Button backButton;
	private Button refreshButton;
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
		UIConnectionWrapper.findCurrentGamesByPlayerHandle(this, GameLoop.getUser().handle);
	}

	@Override
	public void render(float delta) {

	}

	private String playerInfoText(List<MinifiedPlayer> otherPlayers) {
		String playerDescription = "";
		for (MinifiedPlayer player : otherPlayers) {
			playerDescription = player.handle + " [" + ConfigResolver.getRankForXp(player.xp).level + "]";
		}
		return playerDescription;
	}

	public void takeActionOnGameboard(MinifiedGame toTakeActionOn, String playerHandle) {
		loadingOverlay = new LoadingOverlay(resources);
		stage.addActor(loadingOverlay);
		UIConnectionWrapper.findGameById(new SelectGameResultHander(), toTakeActionOn.id, GameLoop.getUser().handle);
	}

	@Override
	public void hide() {
		this.stage = null;
		if (waitImage != null) {
			waitImage.stop();
		}
		for (Actor actor : actors) {
			actor.remove();
		}
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
		if (loadingOverlay != null) {
			loadingOverlay.remove();
		}

		returnValue = null;
		allGames = null;
		allGames = result;

		waitImage.stop();

		showGames();
	}

	private void showGames() {
		scrollList.clearRows();
		if (loadingOverlay != null) {
			loadingOverlay.remove();
		}
		if (allGames == null) {
			return;
		}

		List<MinifiedGame> games = new ArrayList<MinifiedGame>(allGames.getAllGames());

		if (games.isEmpty()) {
			messageLabel.setText("No games available");
		} else {
			Collections.sort(games, new Comparator<MinifiedGame>() {
				@Override
				public int compare(MinifiedGame o1, MinifiedGame o2) {
					if (o1.hasWinner(true) && !o2.hasWinner(true)) {
						return 1;
					} else if (!o1.hasWinner(true) && o2.hasWinner(true)) {
						return -1;
					} else if (o1.moveAvailable && o2.moveAvailable) {
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
						takeActionOnGameboard(game, GameLoop.getUser().handle);
					}
				});
			}
		}
	}

	private void createGameEntry(MinifiedGame game, Group group) {
		float width = group.getWidth();
		float rowHeight = group.getHeight();

		String opponent;
		List<MinifiedPlayer> otherPlayers = game.allPlayersExcept(GameLoop.getUser().handle);
		if (otherPlayers.size() == 0) {
			opponent = BoardScreen.Labels.waitingLabel(game.social);
		} else {
			opponent = "vs " + playerInfoText(otherPlayers);
		}

		ShaderLabel vsLabel = new ShaderLabel(resources.fontShader, opponent, resources.skin,
				Constants.UI.DEFAULT_FONT, Color.WHITE);
		vsLabel.setAlignment(Align.center);
		vsLabel.setWidth(width);
		vsLabel.setY(rowHeight * 0.6f);
		group.addActor(vsLabel);

		String statusText = "";
		Color color = Color.GREEN;
		if (game.hasBeenDeclined()) {
			statusText = "-- Invite Declined --";
			color = Color.RED;
		} else if (game.hasWinner(false)) {
			if (game.hasWinner(true)) {
				if (game.winner.equals(GameLoop.getUser().handle)) {
					statusText = "You Won";
				} else {
					statusText = "You Lost";
					color = Color.RED;
				}
			} else {
				statusText = "--view winner--";
				color = Color.YELLOW;
			}
		} else if (game.moveAvailable) {
			statusText = "--your move--";
			color = Color.YELLOW;
		} else if (game.claimAvailable) {
			statusText = "--claim available--";
			color = Color.YELLOW;
		}

		if (!statusText.isEmpty()) {
			ShaderLabel yourMoveLabel = new ShaderLabel(resources.fontShader, statusText, resources.skin,
					Constants.UI.DEFAULT_FONT, color);
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
				Constants.UI.X_SMALL_FONT, Color.WHITE);
		mapLabel.setAlignment(Align.left);
		mapLabel.setWidth(width);
		mapLabel.setY(rowHeight * 0.15f);
		mapLabel.setX(width * 0.08f);

		group.addActor(mapLabel);
	}

	@Override
	public void onConnectionError(String msg) {
		if (loadingOverlay != null) {
			loadingOverlay.remove();
		}
		returnValue = null;
		allGames = null;

		waitImage.stop();
		final Overlay overlay = new DismissableOverlay(resources, new TextOverlay(CONNECTION_ERROR_MESSAGE, resources));
		stage.addActor(overlay);
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
			final Overlay overlay = new DismissableOverlay(resources, new TextOverlay(CONNECTION_ERROR_MESSAGE,
					resources));
			stage.addActor(overlay);
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
	public void resize(int width, int height) {
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);

		GraphicsUtils.setCommonButtonSize(backButton);
		backButton.setX(10);
		backButton.setY(height - backButton.getHeight() - 5);

		messageLabel.setWidth(width);
		messageLabel.setX(width / 2 - messageLabel.getWidth() / 2);
		messageLabel.setY(0.45f * height);

		float tableHeight = height * 0.89f;
		scrollList.setX(0);
		scrollList.setY(0);
		scrollList.setWidth(width);
		scrollList.setHeight(tableHeight);

		GraphicsUtils.setCommonButtonSize(refreshButton);
		refreshButton.setX(Gdx.graphics.getWidth() - (refreshButton.getWidth() * 1.1f));
		refreshButton.setY(height - refreshButton.getHeight() - 5);
	}

	@Override
	public void show(Stage stage) {
		actors.clear();

		waitImage = new WaitImageButton(resources.skin);

		stage.addActor(waitImage);

		createBackButton(stage);
		createRefreshButton(stage);

		messageLabel = new ShaderLabel(resources.fontShader, "", resources.skin, Constants.UI.DEFAULT_FONT, Color.WHITE);
		messageLabel.setAlignment(Align.center);

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

		scrollList = new ScrollList<MinifiedGame>(resources.skin) {
			@Override
			public void buildCell(MinifiedGame item, Group group) {
				createGameEntry(item, group);
			}
		};

		actors.add(scrollList);
		stage.addActor(scrollList);

		this.stage = stage;

		waitImage.start();
		UIConnectionWrapper.findAllMaps(mapResultCallback);
	}

	private void createBackButton(Stage stage) {
		backButton = new Button(resources.skin, "backButton");
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
	}

	private void createRefreshButton(final Stage stage) {
		refreshButton = new Button(resources.skin, "refreshButton");
		refreshButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				loadingOverlay = new LoadingOverlay(resources);
				stage.addActor(loadingOverlay);
				UIConnectionWrapper.findCurrentGamesByPlayerHandle(GameListScreen.this, GameLoop.getUser().handle);
			}
		});
		actors.add(refreshButton);
		stage.addActor(refreshButton);
	}

	@Override
	public boolean hideTitleArea() {
		return true;
	}

	private UIConnectionResultCallback<Maps> mapResultCallback = new UIConnectionResultCallback<Maps>() {

		@Override
		public void onConnectionResult(Maps result) {
			allMaps = result;
			UIConnectionWrapper.findCurrentGamesByPlayerHandle(GameListScreen.this, GameLoop.getUser().handle);
		}

		@Override
		public void onConnectionError(String msg) {
			GameListScreen.this.onConnectionError(msg);
		}
	};

	@Override
	public boolean canRefresh() {
		return true;
	}

}

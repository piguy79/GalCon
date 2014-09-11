package com.railwaygames.solarsmash.screen;

import static com.railwaygames.solarsmash.Constants.CONNECTION_ERROR_MESSAGE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.PartialScreenFeedback;
import com.railwaygames.solarsmash.UIConnectionWrapper;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.GameQueue;
import com.railwaygames.solarsmash.model.GameQueueItem;
import com.railwaygames.solarsmash.model.Map;
import com.railwaygames.solarsmash.model.Maps;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.model.Size;
import com.railwaygames.solarsmash.screen.event.InviteEventListener;
import com.railwaygames.solarsmash.screen.overlay.DismissableOverlay;
import com.railwaygames.solarsmash.screen.overlay.LoadingOverlay;
import com.railwaygames.solarsmash.screen.overlay.Overlay;
import com.railwaygames.solarsmash.screen.overlay.TextOverlay;
import com.railwaygames.solarsmash.screen.widget.ActionButton;
import com.railwaygames.solarsmash.screen.widget.GameInviteGroup;
import com.railwaygames.solarsmash.screen.widget.ScrollList;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;
import com.railwaygames.solarsmash.screen.widget.WaitImageButton;

public class GameQueueScreen implements PartialScreenFeedback {
	private Stage stage;
	private Resources resources;

	private Object returnCode = null;

	private ScrollList<GameQueueItem> scrollList;
	private ActionButton backButton;
	private ActionButton refreshButton;
	protected WaitImageButton waitImage;
	private ShaderLabel messageLabel;
	private Maps allMaps;
	private LoadingOverlay loadingOverlay;

	private Array<Actor> actors = new Array<Actor>();

	public PartialScreenFeedback previousScreen;

	public GameQueueScreen(Resources resources) {
		this.resources = resources;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();
	}

	private void initialize() {
		createWaitImage();
		createMessageLabel();
		createBackButton();
		createRefreshButton();
		createScrollList();
		showQueueItems();

		Gdx.input.setInputProcessor(stage);
	}

	private void createMessageLabel() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		messageLabel = new ShaderLabel(resources.fontShader, "", resources.skin, Constants.UI.DEFAULT_FONT, Color.WHITE);
		messageLabel.setAlignment(Align.center);
		messageLabel.setWidth(width);
		messageLabel.setX(width / 2 - messageLabel.getWidth() / 2);
		messageLabel.setY(0.45f * height);

		actors.add(messageLabel);
		stage.addActor(messageLabel);

	}

	private void createWaitImage() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		waitImage = new WaitImageButton(resources.skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);
	}

	private void createBackButton() {
		Point position = new Point(10, 0);
		backButton = new ActionButton(resources.skin, "backButton", position);
		GraphicsUtils.setCommonButtonSize(backButton);
		backButton.setX(position.x);
		backButton.setY(Gdx.graphics.getHeight() - backButton.getHeight() - 5);
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				startHideSequence(Action.BACK);
			}
		});
		actors.add(backButton);
		stage.addActor(backButton);
	}

	private void createRefreshButton() {
		Point position = new Point(10, 0);
		refreshButton = new ActionButton(resources.skin, "refreshButton", position);
		GraphicsUtils.setCommonButtonSize(refreshButton);
		refreshButton.setX(Gdx.graphics.getWidth() - (refreshButton.getWidth() * 1.1f));
		refreshButton.setY(Gdx.graphics.getHeight() - refreshButton.getHeight() - 5);
		refreshButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				loadingOverlay = new LoadingOverlay(resources);
				stage.addActor(loadingOverlay);
				UIConnectionWrapper.findPendingInvites(gamequeueCallback, GameLoop.getUser().handle);
			}
		});
		actors.add(refreshButton);
		stage.addActor(refreshButton);
	}

	private void createScrollList() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		final float tableHeight = height - (height * 0.05f);
		scrollList = new ScrollList<GameQueueItem>(resources.skin) {
			@Override
			public void buildCell(GameQueueItem item, Group group) {
				createGameQueueItemEntry(item, group);
			}
		};
		scrollList.setX(0);
		scrollList.setY(-backButton.getHeight());
		scrollList.setWidth(width);
		scrollList.setHeight(tableHeight);

		actors.add(scrollList);
		stage.addActor(scrollList);

	}

	private void startHideSequence(final Object retVal) {
		waitImage.stop();
		GraphicsUtils.hideAnimated(actors, retVal.equals(Action.BACK), new Runnable() {
			@Override
			public void run() {
				returnCode = retVal;
			}
		});
	}

	private void createGameQueueItemEntry(final GameQueueItem item, Group group) {
		Map map = allMaps.getMap(item.game.mapKey);

		GameInviteGroup inviteGroup = new GameInviteGroup(resources, item,
				new Size(group.getWidth(), group.getHeight()), map);

		inviteGroup.addListener(new InviteEventListener() {
			@Override
			public void inviteDeclineFail() {
				final Overlay overlay = new DismissableOverlay(resources, new TextOverlay("Unable to decline invite.",
						resources), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						UIConnectionWrapper.findPendingInvites(gamequeueCallback, GameLoop.getUser().handle);
					}
				});
				stage.addActor(overlay);
			}

			@Override
			public void inviteDeclineSuccess() {
				showQueueItems();
			}

			@Override
			public void inviteAcceptedFail(String errorMessage) {
				final Overlay overlay = new DismissableOverlay(resources, new TextOverlay("Unable to load game.",
						resources), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						UIConnectionWrapper.findPendingInvites(gamequeueCallback, GameLoop.getUser().handle);
					}
				});
				stage.addActor(overlay);
			}

			@Override
			public void inviteAcceptedSuccess(GameBoard gameBoard) {
				returnCode = gameBoard;
			}

			@Override
			public void noCoins() {
				returnCode = Action.NO_MORE_COINS;
			}
		});
		group.addActor(inviteGroup);

	}

	private void displayQueue(GameQueue result) {
		for (final GameQueueItem item : result.gameQueueItems) {
			scrollList.addRow(item, new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {

				}
			});
		}
	}

	protected class SelectGameResultHander implements UIConnectionResultCallback<GameBoard> {

		@Override
		public void onConnectionResult(GameBoard result) {
			loadingOverlay.remove();
			returnCode = result;
		}

		@Override
		public void onConnectionError(String msg) {
			loadingOverlay.remove();
			final Overlay overlay = new DismissableOverlay(resources,
					new TextOverlay("Unable to load game.", resources), new ClickListener() {
						@Override
						public void clicked(InputEvent event, float x, float y) {
							UIConnectionWrapper.findPendingInvites(gamequeueCallback, GameLoop.getUser().handle);
						}
					});
			stage.addActor(overlay);
		}
	}

	private void showQueueItems() {
		waitImage.start();
		scrollList.clearRows();
		UIConnectionWrapper.findAllMaps(mapResultCallback);
	}

	@Override
	public void hide() {
		for (Actor actor : actors) {
			actor.remove();
		}
	}

	@Override
	public Object getRenderResult() {
		return returnCode;
	}

	@Override
	public void resetState() {
		returnCode = null;
	}

	private UIConnectionResultCallback<GameQueue> gamequeueCallback = new UIConnectionResultCallback<GameQueue>() {

		@Override
		public void onConnectionResult(GameQueue result) {
			waitImage.stop();
			scrollList.clearRows();
			if (loadingOverlay != null) {
				loadingOverlay.remove();
			}
			if (result == null || result.gameQueueItems.isEmpty()) {
				messageLabel.setText("No invites found.");
			} else {
				messageLabel.setText("");
				displayQueue(result);
			}
		}

		@Override
		public void onConnectionError(String msg) {
			waitImage.stop();
			final Overlay overlay = new DismissableOverlay(resources, new TextOverlay(CONNECTION_ERROR_MESSAGE,
					resources));
			stage.addActor(overlay);

		}
	};

	public void resize(int width, int height) {

	};

	@Override
	public void show(Stage stage) {
		actors.clear();
		this.stage = stage;
		initialize();
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public boolean hideTitleArea() {

		return true;
	}

	private UIConnectionResultCallback<Maps> mapResultCallback = new UIConnectionResultCallback<Maps>() {

		@Override
		public void onConnectionResult(Maps result) {
			allMaps = result;
			UIConnectionWrapper.findPendingInvites(gamequeueCallback, GameLoop.getUser().handle);
		}

		@Override
		public void onConnectionError(String msg) {
			waitImage.stop();
			final Overlay overlay = new DismissableOverlay(resources, new TextOverlay(CONNECTION_ERROR_MESSAGE,
					resources));
			stage.addActor(overlay);
		}
	};

	@Override
	public boolean canRefresh() {
		return true;
	}

}

package com.xxx.galcon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.BaseResult;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.GameQueue;
import com.xxx.galcon.model.GameQueueItem;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.overlay.LoadingOverlay;
import com.xxx.galcon.screen.widget.ActionButton;
import com.xxx.galcon.screen.widget.ScrollList;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class GameQueueScreen implements PartialScreenFeedback {
	private InputProcessor oldInputProcessor;

	private Stage stage;
	private Resources resources;

	private Object returnCode = null;

	private ScrollList<GameQueueItem> scrollList;
	private ActionButton backButton;
	protected WaitImageButton waitImage;
	private ShaderLabel messageLabel;
	private Maps allMaps;
	private LoadingOverlay loadingOverlay;


	private Array<Actor> actors = new Array<Actor>();

	public GameQueueScreen(Resources resources) {
		this.resources = resources;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();
	}

	private void initialize() {
		createWaitImage();
		createMessageLabel();
		createBackButton();
		createScrollList();
		showQueueItems();

		Gdx.input.setInputProcessor(stage);
	}

	private void createMessageLabel() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		messageLabel = new ShaderLabel(resources.fontShader, "", resources.skin, Constants.UI.DEFAULT_FONT);
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
		float width = Gdx.graphics.getWidth();

		ShaderLabel vsLabel = new ShaderLabel(resources.fontShader, "vs ", resources.skin, Constants.UI.DEFAULT_FONT);
		vsLabel.setAlignment(Align.center);
		vsLabel.setWidth(width);
		vsLabel.setY(group.getHeight() * 0.6f);

		group.addActor(vsLabel);

		ShaderLabel playerLabel = new ShaderLabel(resources.fontShader, item.requester.handle, resources.skin,
				Constants.UI.DEFAULT_FONT);
		playerLabel.setAlignment(Align.center);
		playerLabel.setWidth(width);
		playerLabel.setY(group.getHeight() * 0.35f);

		group.addActor(playerLabel);

		final ShaderLabel levelLabel = new ShaderLabel(resources.fontShader, " Map: " + allMaps.getMap(item.game.mapKey).title,
				resources.skin, Constants.UI.DEFAULT_FONT);
		levelLabel.setAlignment(Align.center);
		levelLabel.setWidth(width);
		levelLabel.setY(group.getHeight() * 0.1f);

		group.addActor(levelLabel);

		float centerY = (group.getHeight() / 2) - (GraphicsUtils.actionButtonSize / 2);
		ActionButton cancelButton = new ActionButton(resources.skin, "cancelButton", new Point(
				GraphicsUtils.actionButtonSize / 2, centerY));
		cancelButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				loadingOverlay = new LoadingOverlay(resources);
				stage.addActor(loadingOverlay);
				UIConnectionWrapper.declineInvite(new UIConnectionResultCallback<BaseResult>() {
					@Override
					public void onConnectionResult(BaseResult result) {
						loadingOverlay.remove();
						showQueueItems();
					}

					@Override
					public void onConnectionError(String msg) {
						loadingOverlay.remove();
						messageLabel.setText("Unable to decline invite.");
					}
				}, item.game.id, GameLoop.USER.handle);
			}
		});
		group.addActor(cancelButton);

		ActionButton okButton = new ActionButton(resources.skin, "okButton", new Point(group.getWidth()
				- (GraphicsUtils.actionButtonSize * 1.5f), centerY));
		okButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				loadingOverlay = new LoadingOverlay(resources);
				UIConnectionWrapper.acceptInvite(new SelectGameResultHander(), item.game.id, GameLoop.USER.handle);
			}
		});
		group.addActor(okButton);

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
			messageLabel.setText(Constants.CONNECTION_ERROR_MESSAGE);
		}
	}

	private void showQueueItems() {
		waitImage.start();
		scrollList.clearRows();
		UIConnectionWrapper.findAllMaps(mapResultCallback);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(oldInputProcessor);
	}

	@Override
	public Object getRenderResult() {
		return returnCode;
	}

	@Override
	public void resetState() {
		returnCode = null;
	}

	@Override
	public void show(Stage stage, float width, float height) {
		actors.clear();
		this.stage = stage;
		initialize();
		oldInputProcessor = Gdx.input.getInputProcessor();
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
			UIConnectionWrapper.findPendingInvites(new UIConnectionResultCallback<GameQueue>() {

				@Override
				public void onConnectionResult(GameQueue result) {
					waitImage.stop();
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
					messageLabel.setText(Constants.CONNECTION_ERROR_MESSAGE);

				}
			}, GameLoop.USER.handle);
		}

		@Override
		public void onConnectionError(String msg) {
			waitImage.stop();
			messageLabel.setText(Constants.CONNECTION_ERROR_MESSAGE);
		}
	};

}

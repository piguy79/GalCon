package com.railwaygames.solarsmash.screen.overlay;

import static com.railwaygames.solarsmash.Constants.CONNECTION_ERROR_MESSAGE;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.UIConnectionWrapper;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.GameQueue;
import com.railwaygames.solarsmash.model.GameQueueItem;
import com.railwaygames.solarsmash.model.Maps;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.model.Size;
import com.railwaygames.solarsmash.screen.Action;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.event.GameReturnEvent;
import com.railwaygames.solarsmash.screen.event.InviteEventListener;
import com.railwaygames.solarsmash.screen.event.TransitionEvent;
import com.railwaygames.solarsmash.screen.widget.CommonTextButton;
import com.railwaygames.solarsmash.screen.widget.GameInviteGroup;
import com.railwaygames.solarsmash.screen.widget.ScrollList;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;
import com.railwaygames.solarsmash.screen.widget.WaitImageButton;

public abstract class EndGameOverlay extends Overlay {

	protected GameBoard gameBoard;
	protected ShaderLabel resultLabel;
	protected ShaderLabel messageLabel;

	private WaitImageButton waitImage;
	private CommonTextButton rematchButton;
	private CommonTextButton backButton;

	private ScrollList<GameQueueItem> scrollList;
	protected float height;
	protected float width;

	private Maps allMaps;

	public EndGameOverlay(Resources resources, GameBoard gameBoard) {
		super(resources);
		this.gameBoard = gameBoard;
		this.height = Gdx.graphics.getHeight();
		this.width = Gdx.graphics.getWidth();

		UIConnectionWrapper.findAllMaps(mapResultCallback);
	}

	private void createOverlay() {
		createResultLabel(getTextForResultLabel());
		createMessageLabel(getTextForMessageLabel());
		createWaitImage();
		if(!gameBoard.ai){
			createRematchSection();
		}else{
			waitImage.stop();
		}
		createBackButton();
	}

	private void createBackButton() {
		float buttonHeight = height * 0.1f;
		float buttonWidth = width * 0.45f;
		backButton = new CommonTextButton(resources.skin, "Main Menu", buttonHeight, buttonWidth, resources.fontShader);
		backButton.setX((width / 2) - (backButton.getWidth() / 2));
		backButton.setY(height * 0.45f);

		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent clickEvent, float x, float y) {
				TransitionEvent event = new TransitionEvent(Action.MAIN_MENU);
				fire(event);
			}
		});
		addActor(backButton);
	}

	protected void createResultLabel(String text) {
		resultLabel = new ShaderLabel(resources.fontShader, text, resources.skin, getResultFont(), getResultColor());
		resultLabel.setX((width / 2) - (resultLabel.getTextBounds().width / 2));
		resultLabel.setY(height * 0.75f);

		addActor(resultLabel);
	};

	private void createMessageLabel(String text) {
		messageLabel = new ShaderLabel(resources.fontShader, text, resources.skin, getMessageFont(), getResultColor());
		messageLabel.setX(width * 0.05f);
		messageLabel.setAlignment(Align.center);
		messageLabel.setY(resultLabel.getY() - (height * 0.15f));
		messageLabel.setWidth(width * 0.9f);
		messageLabel.setWrap(true);

		addActor(messageLabel);
	}

	private void createWaitImage() {
		waitImage = new WaitImageButton(resources.skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX((width / 2) - (buttonWidth / 2));
		waitImage.setY(height * 0.3f);

		addActor(waitImage);

		waitImage.start();
	}

	private UIConnectionResultCallback<GameQueue> gameQueueCallback = new UIConnectionResultCallback<GameQueue>() {

		@Override
		public void onConnectionResult(GameQueue result) {
			waitImage.stop();
			List<GameQueueItem> invites = new ArrayList<GameQueueItem>();

			for (GameQueueItem item : result.gameQueueItems) {
				if (item.requester.handle.equals(opponent().handle)) {
					invites.add(item);
				}
			}
			if (invites.isEmpty() && GameLoop.USER.coins > 0) {
				if (scrollList != null) {
					scrollList.remove();
				}
				createRematchButton();
			} else {
				createInviteScrollList(invites);
			}

		}

		@Override
		public void onConnectionError(String msg) {
			waitImage.stop();
			createRematchButton();

		}
	};

	private void createRematchSection() {
		UIConnectionWrapper.findPendingInvites(gameQueueCallback, GameLoop.USER.handle);
	}

	private void createInviteScrollList(List<GameQueueItem> invites) {
		scrollList = new ScrollList<GameQueueItem>(resources.skin) {
			@Override
			public void buildCell(GameQueueItem item, Group group) {
				createGameQueueItemEntry(item, group);
			}
		};
		scrollList.setX(0);
		scrollList.setY(height * 0.05f);
		scrollList.setWidth(width);
		scrollList.setHeight(height * 0.4f);

		addActor(scrollList);

		for (GameQueueItem item : invites) {
			scrollList.addRow(item, new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
				}
			});
		}
	}

	private Player opponent() {
		return gameBoard.allPlayersExcept(GameLoop.USER.handle).get(0);
	}

	private void createGameQueueItemEntry(final GameQueueItem item, Group group) {
		GameInviteGroup inviteGroup = new GameInviteGroup(resources, item,
				new Size(group.getWidth(), group.getHeight()), allMaps.getMap(item.game.mapKey));
		inviteGroup.addListener(new InviteEventListener() {
			@Override
			public void inviteDeclineFail() {
				final Overlay overlay = new DismissableOverlay(resources, new TextOverlay("Unable to decline invite.",
						resources), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						UIConnectionWrapper.findPendingInvites(gameQueueCallback, GameLoop.USER.handle);
					}
				});
				addActor(overlay);
			}

			@Override
			public void inviteDeclineSuccess() {
				UIConnectionWrapper.findPendingInvites(gameQueueCallback, GameLoop.USER.handle);
			}

			@Override
			public void inviteAcceptedFail(String errorMessage) {
				final Overlay overlay = new DismissableOverlay(resources, new TextOverlay("Unable to join game.",
						resources), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						UIConnectionWrapper.findPendingInvites(gameQueueCallback, GameLoop.USER.handle);
					}
				});
				addActor(overlay);
			}

			@Override
			public void inviteAcceptedSuccess(GameBoard gameBoard) {
				fire(new GameReturnEvent(gameBoard.id));
			}
		});
		group.addActor(inviteGroup);

	}

	private ClickListener rematchClickListener = new ClickListener() {
		public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
			rematchButton.remove();
			waitImage.start();
			UIConnectionWrapper.invitePlayerForGame(new UIConnectionResultCallback<GameBoard>() {
				public void onConnectionResult(GameBoard result) {
					waitImage.stop();
					fire(new GameReturnEvent(result.id));
				};

				public void onConnectionError(String msg) {
					waitImage.stop();
					Overlay errorOverlay = new DismissableOverlay(resources, new TextOverlay(msg, resources), null);
					addActor(errorOverlay);
				};

			}, GameLoop.USER.handle, gameBoard.allPlayersExcept(GameLoop.USER.handle).get(0).handle, gameBoard.map);
		};
	};

	public void createRematchButton() {
		float buttonHeight = height * 0.1f;
		float buttonWidth = width * 0.45f;
		rematchButton = new CommonTextButton(resources.skin, "Rematch", buttonHeight, buttonWidth, resources.fontShader);
		rematchButton.setX((width / 2) - (rematchButton.getWidth() / 2));
		rematchButton.setY(height * 0.3f);

		rematchButton.addListener(rematchClickListener);
		addActor(rematchButton);
	}

	abstract String getTextForResultLabel();

	abstract String getTextForMessageLabel();

	abstract String getResultFont();

	abstract Color getResultColor();

	abstract String getMessageFont();

	private UIConnectionResultCallback<Maps> mapResultCallback = new UIConnectionResultCallback<Maps>() {

		@Override
		public void onConnectionResult(Maps result) {
			allMaps = result;
			createOverlay();
		}

		@Override
		public void onConnectionError(String msg) {
			waitImage.stop();
			final Overlay overlay = new DismissableOverlay(resources, new TextOverlay(CONNECTION_ERROR_MESSAGE,
					resources));
			addActor(overlay);
		}
	};

}

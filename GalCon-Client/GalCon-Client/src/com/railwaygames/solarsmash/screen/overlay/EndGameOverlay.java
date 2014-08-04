package com.railwaygames.solarsmash.screen.overlay;

import static com.railwaygames.solarsmash.Constants.CONNECTION_ERROR_MESSAGE;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.Constants;
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
import com.railwaygames.solarsmash.screen.GraphicsUtils;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.event.GameReturnEvent;
import com.railwaygames.solarsmash.screen.event.InviteEventListener;
import com.railwaygames.solarsmash.screen.event.TransitionEvent;
import com.railwaygames.solarsmash.screen.widget.CoinInfoDisplay;
import com.railwaygames.solarsmash.screen.widget.CommonCoinButton;
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
	private CommonCoinButton rematchButton;
	private CommonTextButton backButton;

	private ScrollList<GameQueueItem> scrollList;
	protected float height;
	protected float width;

	private Maps allMaps;
	
	private ShaderLabel tipLabel;
	
	private boolean fadeComplete = false;
	private GameBoard boardToPlay = null;
	
	private List<Tip> tips = new ArrayList<Tip>(){
		{
			add(new Tip(0L, "The best defence is a good offense"));
			add(new Tip(0L, "Planets with larger build rates and a low number of ships make great targets"));
			add(new Tip(0L, "Invite your friends using the 'Friends' button when selecting a map"));
			add(new Tip(0L, "Buy any coin pack to remove Ads. This will enable you to have more games in progress"));
			add(new Tip(0L, "You can move ships between your own planets in order to bolster their defence"));
			add(new Tip(0L, "Use the number of rounds a ship takes to move to time fleets from multiple planets to attack together."));
			add(new Tip(0L,"Larger planets have a higher ship build rate. A higher overall build rate will help you overrun the enemy"));
			add(new Tip(2L,"Planets with moons convey special abilities but have a build rate of 1. Capture them wisely"));
			add(new MapSpecificTip(3L, "Increased speed enables your ships to move from planet to planet in less turns"));
			add(new MapSpecificTip(6L,"Harvest an ability planets moon to gain an increase in power. This will destory the moon after a number of rounds"));
			add(new MapSpecificTip(6L,"Capturing a planet which is under harvest from the enemy will give you a ship bonus."));

		}
	};

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
		createTipLabel();
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
			
			final Overlay coverBoard = new Overlay(resources, 1);
			getStage().addActor(coverBoard);
			final CoinInfoDisplay display = new CoinInfoDisplay(resources, rematchButton.getCoinImage());
			display.animate(new Runnable() {
				
				@Override
				public void run() {
					fadeComplete = true;
					if(boardToPlay != null){
						fire(new GameReturnEvent(boardToPlay.id));
					}
				}
			});
			
			getStage().addActor(display.getCoinImage());
			getStage().addActor(display.getCoinAmountText());
			
			addAction(Actions.fadeOut(1));
			UIConnectionWrapper.invitePlayerForGame(new UIConnectionResultCallback<GameBoard>() {
				public void onConnectionResult(GameBoard result) {
					boardToPlay = result;
					if(fadeComplete){
						fire(new GameReturnEvent(result.id));
					}
				};

				public void onConnectionError(String msg) {
					Overlay errorOverlay = new DismissableOverlay(resources, new TextOverlay(msg, resources), new ClickListener(){
						public void clicked(InputEvent event, float x, float y) {
							fire(new TransitionEvent(Action.MAIN_MENU));
						};
					});
					addActor(errorOverlay);
					
				};

			}, GameLoop.USER.handle, gameBoard.allPlayersExcept(GameLoop.USER.handle).get(0).handle, gameBoard.map);
		};
	};

	public void createRematchButton() {
		float buttonHeight = height * 0.1f;
		float buttonWidth = width * 0.45f;
		rematchButton = new CommonCoinButton(resources.skin, "Rematch", buttonHeight, buttonWidth, resources.fontShader);
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
	
	private void createTipLabel() {
		tipLabel = new ShaderLabel(resources.fontShader, "Tip: " + getTip(gameBoard.map).tip, resources.skin, Constants.UI.SMALL_FONT, Color.WHITE);
		tipLabel.setBounds(Gdx.graphics.getWidth() * 0.05f,Gdx.graphics.getHeight() * 0.15f, Gdx.graphics.getWidth() * 0.9f, tipLabel.getTextBounds().height);
		tipLabel.setWrap(true);
		tipLabel.setAlignment(Align.center);
		
		addActor(tipLabel);
		
	}
	
	private Tip getTip(Long map) {
		List<Tip> tipsToPickFrom = new ArrayList<EndGameOverlay.Tip>();
		
		for(Tip tip : tips){
			if(tip.shouldShowTip(map)){
				tipsToPickFrom.add(tip);
			}
		}
		int index = (int)(Math.random() * tipsToPickFrom.size());
		return tipsToPickFrom.get(index);
	}
	
	private class Tip{
		private Long mapFrom;
		private String tip;
		
		public Tip(Long mapFrom, String tip){
			this.mapFrom = mapFrom;
			this.tip = tip;
		}
		
		public boolean shouldShowTip(Long map){
			return mapFrom <= map;
		}
	}
	
	private class MapSpecificTip extends Tip{
		private Long mapMatch;
		
		public MapSpecificTip(Long mapMatch, String tip){
			super(0L, tip);
			this.mapMatch = mapMatch;
		}
		
		@Override
		public boolean shouldShowTip(Long map){
			return map == mapMatch;
		}
	}

}

package com.xxx.galcon.screen;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ExternalActionWrapper;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.InAppBillingAction.Callback;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.screen.hud.HeaderHud;
import com.xxx.galcon.screen.overlay.DismissableOverlay;
import com.xxx.galcon.screen.overlay.Overlay;
import com.xxx.galcon.screen.overlay.TextOverlay;
import com.xxx.galcon.screen.widget.ScrollList;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.ShaderTextButton;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class NoMoreCoinsDialog implements PartialScreenFeedback, UIConnectionResultCallback<Player> {
	private Stage stage;
	private Array<Actor> actors = new Array<Actor>();

	private ShaderLabel timeRemainingText;
	private ShaderLabel coinText;
	protected WaitImageButton waitImage;
	protected Overlay purchaseOverlay;
	private Group coinGroup;

	private InventoryItem lastPurchaseAttemptItem;

	private Object returnValue;

	private Inventory inventoryResult;

	private Resources resources;

	public NoMoreCoinsDialog(Resources resources) {
		super();
		this.resources = resources;
	}

	private void createBackButton(final Stage stage, final float width, final float height) {
		int buttonHeight = (int) (Gdx.graphics.getHeight() * (HeaderHud.HEADER_HEIGHT_RATIO * 0.88f));
		ImageButton backButton = new ImageButton(resources.skin, "backButton");
		backButton.setX(10);
		backButton.setY(height - buttonHeight - 5);
		backButton.setWidth(buttonHeight);
		backButton.setHeight(buttonHeight);

		backButton.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				startHideSequence(Action.BACK);
			}
		});
		actors.add(backButton);
		stage.addActor(backButton);
	}

	private void createHelpButton(final Stage stage, final float width, final float height) {
		int buttonHeight = (int) (Gdx.graphics.getHeight() * (HeaderHud.HEADER_HEIGHT_RATIO * 0.88f));
		ImageButton helpButton = new ImageButton(resources.skin, Constants.UI.QUESTION_MARK);
		helpButton.setX(width - buttonHeight - 10);
		helpButton.setY(height - buttonHeight - 5);
		helpButton.setWidth(buttonHeight);
		helpButton.setHeight(buttonHeight);

		helpButton.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				final Overlay ovrlay = new DismissableOverlay(
						resources,
						new TextOverlay(
								"Free Coins\n\nFree coins are available after all games in progress have been completed. Upon completion, a cooldown period begins until the free coins are credited.\n\nWatch Ad\n\nWhen the cooldown timer is running, clicking on the watch ad button will reduce the cooldown by 50%.",
								resources), null);
				stage.addActor(ovrlay);
			}
		});
		actors.add(helpButton);
		stage.addActor(helpButton);
	}

	private void createLayout(Stage stage, Inventory stock) {
		final float height = Gdx.graphics.getHeight();
		final float width = Gdx.graphics.getWidth();

		coinGroup = new Group();
		coinGroup.setBounds(0, height * 0.75f, width, height * 0.15f);

		if (GameLoop.USER.usedCoins != -1) {
			addTimeRemainingText(coinGroup);
		} else {
			addCoinImageGroup(coinGroup);
		}

		actors.add(coinGroup);
		stage.addActor(coinGroup);

		Actor inAppBillingTable = createInAppBillingButtons(stock.inventory);
		actors.add(inAppBillingTable);
		stage.addActor(inAppBillingTable);

		createBackButton(stage, width, height);
		createHelpButton(stage, width, height);
	}

	private String coinString(Integer coins) {
		if (coins == null) {
			return "";
		}
		return coins == 1 ? "Coin" : "Coins";
	}

	private Actor createInAppBillingButtons(List<InventoryItem> inventory) {
		ScrollList<InventoryItem> scrollList = new ScrollList<InventoryItem>(resources.skin) {
			@Override
			public void buildCell(InventoryItem item, Group group) {
				float width = group.getWidth();
				float rowHeight = group.getHeight();

				String itemText = item.price + "  " + item.numCoins + " " + coinString(item.numCoins);

				ShaderLabel coinLabel = new ShaderLabel(resources.fontShader, itemText, resources.skin,
						Constants.UI.DEFAULT_FONT);
				coinLabel.setAlignment(Align.center);
				coinLabel.setWidth(width);
				coinLabel.setY(rowHeight * 0.4f);
				group.addActor(coinLabel);
			}
		};
		final float height = Gdx.graphics.getHeight();
		final float width = Gdx.graphics.getWidth();
		scrollList.setBounds(0, 0, width, height * 0.75f);

		Collections.sort(inventory, new Comparator<InventoryItem>() {
			@Override
			public int compare(InventoryItem o1, InventoryItem o2) {
				if (o1.numCoins < o2.numCoins) {
					return -1;
				} else if (o1.numCoins > o2.numCoins) {
					return 1;
				}
				return 0;
			}
		});

		for (final InventoryItem item : inventory) {
			if (item.isAvailable()) {
				scrollList.addRow(item, new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						purchaseOverlay = new TextOverlay("Purchasing...", resources);
						stage.addActor(purchaseOverlay);

						lastPurchaseAttemptItem = item;
						ExternalActionWrapper.purchaseCoins(item, coinsCallback);
					}
				});
			}
		}

		return scrollList;
	}

	private Callback coinsCallback = new Callback() {
		@Override
		public void onSuccess(String msg) {
			purchaseOverlay.remove();
			purchaseOverlay = null;
			lastPurchaseAttemptItem = null;

			if (msg.equals(Constants.CANCELED)) {
				// do nothing for now
			} else {
				coinGroup.clear();
				addCoinImageGroup(coinGroup);
				final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(
						"Coin purchase succeeded!\n\nGo forth and conquer.", resources), null);

				stage.addActor(ovrlay);
			}
		}

		@Override
		public void onFailure(String msg) {
			purchaseOverlay.remove();
			purchaseOverlay = null;

			final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(
					"Could not complete purchase.\n\nPlease try again.", resources), new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					ExternalActionWrapper.purchaseCoins(lastPurchaseAttemptItem, coinsCallback);
				}
			});

			stage.addActor(ovrlay);
		}
	};

	private void addCoinImageGroup(Group group) {
		final float width = Gdx.graphics.getWidth();

		ImageButton coinImage = new ImageButton(resources.skin, Constants.UI.COIN);
		float coinSize = group.getHeight() * 0.95f;
		coinImage.setLayoutEnabled(false);
		coinImage.setWidth(coinSize);
		coinImage.setHeight(coinSize);
		coinImage.setY((group.getHeight() - coinSize) / 2);

		group.addActor(coinImage);

		coinText = new ShaderLabel(resources.fontShader, GameLoop.USER.coins.toString(), resources.skin,
				Constants.UI.LARGE_FONT);
		coinText.setAlignment(Align.right, Align.right);
		float yMidPoint = coinImage.getY() + coinImage.getHeight() / 2;
		float coinTextWidth = coinText.getWidth() * 4;
		coinText.setBounds(width * 0.5f - coinImage.getWidth() / 2 - coinTextWidth, yMidPoint - coinText.getHeight()
				/ 2, coinTextWidth, coinText.getHeight());
		coinImage.setX(coinText.getX() + coinText.getWidth() + 0.05f * width);
		group.addActor(coinText);
	}

	private void addTimeRemainingText(Group group) {
		timeRemainingText = new ShaderLabel(resources.fontShader, findTimeRemaining(), resources.skin,
				Constants.UI.LARGE_FONT);
		float yMidPoint = group.getHeight() * 0.5f;
		float timeTextWidth = timeRemainingText.getWidth() * 1.5f;
		timeRemainingText.setAlignment(Align.center, Align.center);
		timeRemainingText.setBounds(group.getWidth() * 0.5f - timeTextWidth * 0.5f,
				yMidPoint - timeRemainingText.getHeight() * 0.48f, timeTextWidth, timeRemainingText.getHeight());
		group.addActor(timeRemainingText);

		final ImageButton watchAd = new ImageButton(resources.skin, Constants.UI.GREEN_BUTTON);
		float adWidth = group.getWidth() * 0.26f;
		float adHeight = adWidth * 0.55f;
		watchAd.setBounds(group.getWidth() * 0.73f, group.getHeight() * 0.5f - adHeight * 0.5f, adWidth, adHeight);

		ShaderTextButton watchAdText = new ShaderTextButton(resources.fontShader, "Watch\nAd", resources.skin,
				Constants.UI.GREEN_BUTTON_TEXT);
		watchAdText.setY(watchAd.getHeight() / 2 - watchAdText.getHeight() * 0.5f);
		watchAdText.setWidth(watchAd.getWidth());

		watchAd.addActor(watchAdText);
		group.addActor(watchAd);
		watchAd.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (GameLoop.USER.usedCoins != -1 && !GameLoop.USER.watchedAd) {
					ExternalActionWrapper.showAd(new AdColonyVideoListener() {

						@Override
						public void onAdColonyVideoStarted() {
						}

						@Override
						public void onAdColonyVideoFinished() {
							watchAd.remove();
							GameLoop.USER.watchedAd = true;
							UIConnectionWrapper.reduceTimeUntilCoins(new SetPlayerResultHandler(GameLoop.USER),
									GameLoop.USER.handle);
						}
					});
				}
			}
		});
	}

	private String findTimeRemaining() {
		DateTime timeRemaining = GameLoop.USER.timeRemainingUntilCoinsAvailable();

		if (timeRemaining == null) {
			return GameLoop.USER.coins + " " + coinString(GameLoop.USER.coins) + " available!";
		}
		return Constants.timeRemainingFormat.format(timeRemaining.toDate());
	}

	@Override
	public void render(float delta) {
		if (timeRemainingText != null) {
			timeRemainingText.setText(findTimeRemaining());
		}

		if (coinText != null) {
			coinText.setText(GameLoop.USER.coins.toString());
		}
	}

	@Override
	public void hide() {

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
	public Object getRenderResult() {
		return returnValue;
	}

	@Override
	public void resetState() {
		returnValue = null;
	}

	@Override
	public void onConnectionResult(Player result) {
		GameLoop.USER = result;
	}

	@Override
	public void onConnectionError(String msg) {

	}

	@Override
	public void show(Stage stage, float width, float height) {
		actors.clear();
		timeRemainingText = null;
		this.stage = stage;

		waitImage = new WaitImageButton(resources.skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		if (inventoryResult == null) {
			waitImage.start();
			loadUser();
		} else {
			inventoryCallback.onConnectionResult(inventoryResult);
		}
	}

	private void loadUser() {
		UIConnectionWrapper.recoverUsedCoinCount(new UIConnectionResultCallback<Player>() {

			@Override
			public void onConnectionResult(Player result) {
				GameLoop.USER = result;
				ExternalActionWrapper.loadInventory(inventoryCallback);
			}

			@Override
			public void onConnectionError(String msg) {
				final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(
						"Could not retrieve user.\n\nPlease try again.", resources), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						loadUser();
					}
				});
				stage.addActor(ovrlay);
			}
		}, GameLoop.USER.handle);
	}

	@Override
	public boolean hideTitleArea() {
		return true;
	}

	private UIConnectionResultCallback<Inventory> inventoryCallback = new UIConnectionResultCallback<Inventory>() {

		@Override
		public void onConnectionResult(Inventory result) {
			waitImage.stop();

			inventoryResult = result;
			createLayout(stage, result);
		}

		@Override
		public void onConnectionError(String msg) {
			waitImage.stop();

			final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(msg, resources),
					new ClickListener() {
						@Override
						public void clicked(InputEvent event, float x, float y) {
							ExternalActionWrapper.loadInventory(inventoryCallback);
						}
					});

			stage.addActor(ovrlay);
		}
	};
}

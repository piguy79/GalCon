package com.railwaygames.solarsmash.screen;

import static com.railwaygames.solarsmash.Constants.GALCON_PREFS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.ExternalActionWrapper;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.PartialScreenFeedback;
import com.railwaygames.solarsmash.ReturnablePartialScreenFeedback;
import com.railwaygames.solarsmash.UIConnectionWrapper;
import com.railwaygames.solarsmash.config.ConfigResolver;
import com.railwaygames.solarsmash.http.InAppBillingAction.Callback;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Inventory;
import com.railwaygames.solarsmash.model.InventoryItem;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.screen.overlay.DismissableOverlay;
import com.railwaygames.solarsmash.screen.overlay.Overlay;
import com.railwaygames.solarsmash.screen.overlay.TextOverlay;
import com.railwaygames.solarsmash.screen.widget.ScrollList;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;
import com.railwaygames.solarsmash.screen.widget.WaitImageButton;

public class NoMoreCoinsDialog implements PartialScreenFeedback, ReturnablePartialScreenFeedback,
		UIConnectionResultCallback<Player> {
	private Stage stage;
	private Array<Actor> actors = new Array<Actor>();

	private ShaderLabel coinText;
	protected WaitImageButton waitImage;
	protected Overlay purchaseOverlay;
	private Group coinGroup;

	private InventoryItem lastPurchaseAttemptItem;

	private Object returnValue;

	private Inventory inventoryResult;

	private Resources resources;

	public PartialScreenFeedback previousScreen;

	private String coinInfoText = "Free Coins\n\n%s free coins will be credited after all games in progress have been completed and you have 0 coins remaining.\n\nAny coin purchase will remove ads.";

	public NoMoreCoinsDialog(Resources resources) {
		super();
		this.resources = resources;
	}

	private void createBackButton(final Stage stage, final float width, final float height) {
		Button backButton = new Button(resources.skin, "backButton");
		GraphicsUtils.setCommonButtonSize(backButton);
		backButton.setX(10);
		backButton.setY(height - backButton.getHeight() - 5);

		backButton.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				startHideSequence(Action.BACK);
			}
		});
		actors.add(backButton);
		stage.addActor(backButton);
	}

	private void createHelpButton(final Stage stage, final float width, final float height) {
		ImageButton helpButton = new ImageButton(resources.skin, Constants.UI.QUESTION_MARK);
		GraphicsUtils.setCommonButtonSize(helpButton);
		helpButton.setX(width - helpButton.getWidth() - 10);
		helpButton.setY(height - helpButton.getHeight() - 5);

		helpButton.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				String formattedInfo = coinInfoText.format(coinInfoText,
						ConfigResolver.getByConfigKey(Constants.Config.FREE_COINS));
				final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(formattedInfo, resources),
						null);
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

		addCoinImageGroup(coinGroup);

		actors.add(coinGroup);
		stage.addActor(coinGroup);

		List<InventoryItem> coinInventory = new ArrayList<InventoryItem>();
		for (InventoryItem item : stock.inventory) {
			if (item.sku.startsWith("coins")) {
				coinInventory.add(item);
			}
		}

		Actor inAppBillingTable = createInAppBillingButtons(coinInventory);
		actors.add(inAppBillingTable);
		stage.addActor(inAppBillingTable);

		createBackButton(stage, width, height);
		createHelpButton(stage, width, height);
		createFirstTimeDialog(stage, width, height);
	}

	private void createFirstTimeDialog(Stage stage2, float width, float height) {
		Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
		String lastAdShownTime = prefs.getString(Constants.NO_COIN_INFO);
		if (GameLoop.USER.coins == 0 && lastAdShownTime.isEmpty()) {
			prefs.putString(Constants.NO_COIN_INFO, "true");
			prefs.flush();
			String formattedInfo = coinInfoText.format(coinInfoText,
					ConfigResolver.getByConfigKey(Constants.Config.FREE_COINS));
			Overlay coinInfo = new DismissableOverlay(resources, new TextOverlay(formattedInfo, resources));
			stage.addActor(coinInfo);
		}

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
						Constants.UI.DEFAULT_FONT, Color.WHITE);
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
						"Coin purchase succeeded!\n\nGo forth and conquer.", resources), new ClickListener() {
					public void clicked(InputEvent event, float x, float y) {
						startHideSequence(Action.BACK);
					};
				});

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
					// ExternalActionWrapper.purchaseCoins(lastPurchaseAttemptItem,
					// coinsCallback);
				}
			});

			stage.addActor(ovrlay);
		}
	};

	private void addCoinImageGroup(Group group) {
		final float width = Gdx.graphics.getWidth();

		Button coinImage = new Button(resources.skin, Constants.UI.COIN);
		float coinSize = group.getHeight() * 0.95f;
		coinImage.setWidth(coinSize);
		coinImage.setHeight(coinSize);
		coinImage.setY((group.getHeight() - coinSize) / 2);

		group.addActor(coinImage);

		coinText = new ShaderLabel(resources.fontShader, GameLoop.USER.coins.toString(), resources.skin,
				Constants.UI.LARGE_FONT, Color.WHITE);
		coinText.setAlignment(Align.right, Align.right);
		float yMidPoint = coinImage.getY() + coinImage.getHeight() / 2;
		float coinTextWidth = coinText.getWidth() * 4;
		coinText.setBounds(width * 0.5f - coinImage.getWidth() / 2 - coinTextWidth, yMidPoint - coinText.getHeight()
				/ 2, coinTextWidth, coinText.getHeight());
		coinImage.setX(coinText.getX() + coinText.getWidth() + 0.05f * width);
		group.addActor(coinText);
	}

	@Override
	public void render(float delta) {
		if (coinText != null) {
			coinText.setText(GameLoop.USER.coins.toString());
		}
	}

	@Override
	public void hide() {
		for (Actor actor : actors) {
			actor.remove();
		}

		if (waitImage != null) {
			waitImage.stop();
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
	public void resize(int width, int height) {
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
	}

	@Override
	public void show(Stage stage) {
		actors.clear();
		this.stage = stage;

		waitImage = new WaitImageButton(resources.skin);

		stage.addActor(waitImage);

		if (inventoryResult == null) {
			waitImage.start();
			loadUser();
		} else {
			inventoryCallback.onConnectionResult(inventoryResult);
		}
	}

	private void loadUser() {
		UIConnectionWrapper.addFreeCoins(new UIConnectionResultCallback<Player>() {

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

	public boolean canRefresh() {
		return true;
	}

	@Override
	public PartialScreenFeedback getPreviousScreen() {
		return previousScreen;
	}

	@Override
	public void setPreviousScreen(PartialScreenFeedback previousScreen) {
		this.previousScreen = previousScreen;

	}
}

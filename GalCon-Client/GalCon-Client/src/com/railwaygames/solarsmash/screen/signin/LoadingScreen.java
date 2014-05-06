package com.railwaygames.solarsmash.screen.signin;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.PartialScreenFeedback;
import com.railwaygames.solarsmash.config.Configuration;
import com.railwaygames.solarsmash.http.GameAction;
import com.railwaygames.solarsmash.http.InAppBillingAction;
import com.railwaygames.solarsmash.http.InAppBillingAction.Callback;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Inventory;
import com.railwaygames.solarsmash.model.InventoryItem;
import com.railwaygames.solarsmash.model.Order;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.screen.Action;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.overlay.DismissableOverlay;
import com.railwaygames.solarsmash.screen.overlay.Overlay;
import com.railwaygames.solarsmash.screen.overlay.TextOverlay;
import com.railwaygames.solarsmash.screen.widget.WaitImageButton;

/**
 * Generic loading screen. Load anything that is needed prior to the main menu.
 */
public class LoadingScreen implements PartialScreenFeedback {

	private WaitImageButton waitImage;
	private Stage stage;

	private InAppBillingAction inAppBillingAction;
	private GameAction gameAction;
	private String returnResult = null;

	private Resources resources;

	private List<Order> ordersToConsume = null;

	public LoadingScreen(Resources resources, GameAction gameAction, InAppBillingAction inAppBillingAction) {
		this.inAppBillingAction = inAppBillingAction;
		this.gameAction = gameAction;
		this.resources = resources;
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
	public void show(final Stage stage) {
		this.stage = stage;

		waitImage = new WaitImageButton(resources.skin);
		stage.addActor(waitImage);

		waitImage.start();

		gameAction.findConfigByType(configCallback, "app");
	}

	private UIConnectionResultCallback<Configuration> configCallback = new UIConnectionResultCallback<Configuration>() {
		@Override
		public void onConnectionResult(Configuration result) {
			GameLoop.CONFIG = result;
			inAppBillingAction.setup(setupCallback);
		}

		@Override
		public void onConnectionError(String msg) {
			final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(
					"Could not connect.\n\nTouch to retry", resources), new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					gameAction.findConfigByType(configCallback, "app");
				}
			});
			stage.addActor(ovrlay);
		}
	};

	private Callback setupCallback = new Callback() {

		@Override
		public void onSuccess(String msg) {
			consumeAnyPurchasedOrders();
		}

		@Override
		public void onFailure(String msg) {
			waitImage.stop();
			if (msg.equals("retry")) {
				final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(
						"Could not connect.\n\nTouch to retry", resources), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						inAppBillingAction.setup(setupCallback);
					}
				});

				stage.addActor(ovrlay);
			} else {
				final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(
						"In-app purchases\nare not present\nand disabled.\n\nTouch to continue.", resources),
						new ClickListener() {
							@Override
							public void clicked(InputEvent event, float x, float y) {
								consumeAnyPurchasedOrders();
							}
						});
				stage.addActor(ovrlay);
			}
		}
	};

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getRenderResult() {
		return returnResult;
	}

	@Override
	public void resetState() {
		returnResult = null;
	}

	@Override
	public boolean hideTitleArea() {
		return false;
	}

	private void consumeAnyPurchasedOrders() {
		gameAction.loadAvailableInventory(new UIConnectionResultCallback<Inventory>() {

			@Override
			public void onConnectionResult(Inventory inventoryResult) {
				inAppBillingAction.loadInventory(inventoryResult, new UIConnectionResultCallback<Inventory>() {

					@Override
					public void onConnectionResult(final Inventory inventory) {

						List<Order> orders = new ArrayList<Order>();
						for (InventoryItem item : inventory.inventory) {
							if (item.unfulfilledOrder != null) {
								orders.addAll(item.unfulfilledOrder);
							}
						}

						if (!orders.isEmpty()) {
							ordersToConsume = orders;
							gameAction.addCoinsForAnOrder(playerCallback, GameLoop.USER.handle, orders);
						} else {
							doneLoad();
						}
					}

					@Override
					public void onConnectionError(String msg) {
						retryAddCoins(msg);
					}
				});
			}

			@Override
			public void onConnectionError(String msg) {
				retryAddCoins(msg);
			}
		});
	}

	private void doneLoad() {
		waitImage.stop();
		ordersToConsume = null;
		returnResult = Action.DONE;
	}

	private UIConnectionResultCallback<Player> playerCallback = new UIConnectionResultCallback<Player>() {
		@Override
		public void onConnectionResult(Player player) {
			GameLoop.USER = player;
			doneLoad();
		};

		@Override
		public void onConnectionError(String msg) {
			retryAddCoins(msg);
		}
	};

	private void retryAddCoins(String msg) {
		waitImage.stop();
		if (ordersToConsume != null) {
			final Overlay ovrlay = new DismissableOverlay(resources, new TextOverlay(msg, resources),
					new ClickListener() {
						@Override
						public void clicked(InputEvent event, float x, float y) {
							gameAction.addCoinsForAnOrder(playerCallback, GameLoop.USER.handle, ordersToConsume);
						}
					});
			stage.addActor(ovrlay);
		}
	}

	@Override
	public boolean canRefresh() {
		return false;
	}

}

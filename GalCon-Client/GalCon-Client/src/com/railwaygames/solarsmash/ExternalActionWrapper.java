package com.railwaygames.solarsmash;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.railwaygames.solarsmash.http.GameAction;
import com.railwaygames.solarsmash.http.InAppBillingAction;
import com.railwaygames.solarsmash.http.InAppBillingAction.Callback;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Inventory;
import com.railwaygames.solarsmash.model.InventoryItem;
import com.railwaygames.solarsmash.model.Leaderboards;
import com.railwaygames.solarsmash.model.Order;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.model.PlayerList;

public class ExternalActionWrapper {

	private static GameAction gameAction;
	private static InAppBillingAction inAppBillingAction;

	private ExternalActionWrapper() {
		super();
	}

	public static void setActions(GameAction gameAction, InAppBillingAction inAppBillingAction) {
		ExternalActionWrapper.gameAction = gameAction;
		ExternalActionWrapper.inAppBillingAction = inAppBillingAction;
	}

	public static void showAd() {
		gameAction.showAd();
	}

	public static void purchaseCoins(final InventoryItem inventoryItem, final Callback callback) {
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

						if (orders.isEmpty()) {
							doPurchaseCoins(inventoryItem, callback);
						} else {
							addCoinsAndConsumeOrder(orders, callback);
						}
					}

					@Override
					public void onConnectionError(String msg) {
						callback.onFailure(msg);
					}
				});
			}

			@Override
			public void onConnectionError(String msg) {
				callback.onFailure(msg);
			}
		});
	}

	private static void doPurchaseCoins(InventoryItem inventoryItem, final Callback callback) {
		inAppBillingAction.purchaseCoins(inventoryItem, new UIConnectionResultCallback<List<Order>>() {
			@Override
			public void onConnectionResult(List<Order> orders) {
				/*
				 * This may indicate that the user canceled the purchase flow.
				 */
				if (orders == null) {
					callback.onSuccess(Constants.CANCELED);
					return;
				}

				addCoinsAndConsumeOrder(orders, callback);
			}

			@Override
			public void onConnectionError(String msg) {
				callback.onFailure(msg);
			}
		});
	}

	public static void addCoinsAndConsumeOrder(final List<Order> orders, final Callback callback) {
		gameAction.addCoinsForAnOrder(new UIConnectionResultCallback<Player>() {
			@Override
			public void onConnectionResult(final Player player) {
				GameLoop.setUser(player);

				inAppBillingAction.consumeOrders(player.consumedOrders, new Callback() {
					@Override
					public void onSuccess(String msg) {
						gameAction.deleteConsumedOrders(new UIConnectionResultCallback<Player>() {
							@Override
							public void onConnectionResult(Player result) {
								callback.onSuccess("");
							}

							@Override
							public void onConnectionError(String msg) {
								callback.onFailure(msg);
							}
						}, GameLoop.getUser().handle, player.consumedOrders);
					}

					@Override
					public void onFailure(String msg) {
						Gdx.app.log("CONSUME", msg);
						callback.onFailure(msg);
					}
				});
			}

			@Override
			public void onConnectionError(String msg) {
				callback.onFailure(msg);
			}
		}, GameLoop.getUser().handle, orders);
	}

	public static void loadInventory(final UIConnectionResultCallback<Inventory> callback) {
		gameAction.loadAvailableInventory(new UIConnectionResultCallback<Inventory>() {

			@Override
			public void onConnectionResult(Inventory result) {
				inAppBillingAction.loadInventory(result, callback);
			}

			@Override
			public void onConnectionError(String msg) {
				callback.onConnectionError(msg);
			}
		});
	}

	public static void findLeaderboardById(UIConnectionResultCallback<Leaderboards> callback, String id) {
		gameAction.findLeaderboardById(callback, id);
	}

	public static void shouldEnableAds(boolean enable) {
		gameAction.shouldEnableAds(enable);
	}

	public static void addProviderToUserWithOverride(UIConnectionResultCallback<PlayerList> callback, String handle,
			String id, String authProvider, String keepSession, String deleteSession) {
		gameAction.addProviderToUserWithOverride(callback, handle, id, authProvider, keepSession, deleteSession);
	}
}

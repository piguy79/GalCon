package com.railwaygames.solarsmash;

import java.util.List;

import com.railwaygames.solarsmash.http.InAppBillingAction;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Inventory;
import com.railwaygames.solarsmash.model.InventoryItem;
import com.railwaygames.solarsmash.model.Order;

public class AndroidInAppBillingAction implements InAppBillingAction {

	private MainActivity activity;

	public AndroidInAppBillingAction(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void loadInventory(final Inventory inventory, final UIConnectionResultCallback<Inventory> callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				activity.loadStoreInventory(inventory, callback);
			}
		});
	}

	@Override
	public void setup(final Callback callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				activity.setupInAppBilling(callback);
			}
		});
	}

	@Override
	public void consumeOrders(final List<Order> orders, final Callback callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				activity.consumeOrders(orders, callback);
			}
		});
	}

	@Override
	public void purchaseCoins(final InventoryItem inventoryItem, final UIConnectionResultCallback<List<Order>> callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				activity.purchaseCoins(inventoryItem, callback);
			}
		});
	}
}

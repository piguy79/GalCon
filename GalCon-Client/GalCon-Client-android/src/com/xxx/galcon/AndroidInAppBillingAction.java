package com.xxx.galcon;

import java.util.List;

import android.app.Activity;

import com.xxx.galcon.http.InAppBillingAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Order;

public class AndroidInAppBillingAction implements InAppBillingAction {

	private Activity activity;

	public AndroidInAppBillingAction(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void loadInventory(final Inventory inventory, final UIConnectionResultCallback<Inventory> callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) activity).loadStoreInventory(inventory, callback);
			}
		});
	}

	@Override
	public void setup(final Callback callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) activity).setupInAppBilling(callback);
			}
		});
	}

	@Override
	public void consumeOrders(final List<Order> orders, final Callback callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) activity).consumeOrders(orders, callback);
			}
		});
	}

	@Override
	public void purchaseCoins(final InventoryItem inventoryItem, final UIConnectionResultCallback<List<Order>> callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) activity).purchaseCoins(inventoryItem, callback);
			}
		});
	}
}

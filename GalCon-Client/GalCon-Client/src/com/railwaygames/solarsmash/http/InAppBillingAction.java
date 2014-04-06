package com.railwaygames.solarsmash.http;

import java.util.List;

import com.railwaygames.solarsmash.model.Inventory;
import com.railwaygames.solarsmash.model.InventoryItem;
import com.railwaygames.solarsmash.model.Order;

public interface InAppBillingAction {

	public void loadInventory(Inventory inventory, UIConnectionResultCallback<Inventory> callback);

	public void setup(Callback callback);

	public void consumeOrders(final List<Order> orders, Callback callback);

	public void purchaseCoins(InventoryItem inventoryItem, UIConnectionResultCallback<List<Order>> callback);

	public interface Callback {
		public void onSuccess(String msg);

		public void onFailure(String msg);
	}
}

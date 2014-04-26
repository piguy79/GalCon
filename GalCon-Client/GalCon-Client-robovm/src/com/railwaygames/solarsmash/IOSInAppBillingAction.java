package com.railwaygames.solarsmash;

import java.util.ArrayList;
import java.util.List;

import com.railwaygames.solarsmash.http.InAppBillingAction;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Inventory;
import com.railwaygames.solarsmash.model.InventoryItem;
import com.railwaygames.solarsmash.model.Order;

public class IOSInAppBillingAction implements InAppBillingAction {

	@Override
	public void loadInventory(Inventory inventory, UIConnectionResultCallback<Inventory> callback) {
		Inventory inv = new Inventory();
		inv.inventory = new ArrayList<InventoryItem>();

		callback.onConnectionResult(inv);
	}

	@Override
	public void setup(Callback callback) {
		callback.onSuccess("");
	}

	@Override
	public void consumeOrders(List<Order> orders, Callback callback) {
		callback.onSuccess("");
	}

	@Override
	public void purchaseCoins(InventoryItem inventoryItem, UIConnectionResultCallback<List<Order>> callback) {
		callback.onConnectionResult(null);
	}
}

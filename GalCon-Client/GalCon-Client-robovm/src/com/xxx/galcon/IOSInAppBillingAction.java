package com.xxx.galcon;

import java.util.ArrayList;
import java.util.List;

import com.xxx.galcon.http.InAppBillingAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Order;

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

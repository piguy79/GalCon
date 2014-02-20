package com.xxx.galcon;

import java.util.List;

import com.xxx.galcon.http.InAppBillingAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Order;

public class IOSInAppBillingAction implements InAppBillingAction {

	@Override
	public void loadInventory(Inventory inventory, UIConnectionResultCallback<Inventory> callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setup(Callback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void consumeOrders(List<Order> orders, Callback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void purchaseCoins(InventoryItem inventoryItem, UIConnectionResultCallback<List<Order>> callback) {
		// TODO Auto-generated method stub

	}

}

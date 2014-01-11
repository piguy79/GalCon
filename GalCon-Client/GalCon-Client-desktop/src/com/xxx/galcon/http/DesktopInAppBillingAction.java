package com.xxx.galcon.http;

import java.util.ArrayList;
import java.util.List;

import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Order;

public class DesktopInAppBillingAction implements InAppBillingAction {

	private GameAction gameAction;

	public DesktopInAppBillingAction(GameAction gameAction) {
		this.gameAction = gameAction;
	}

	@Override
	public void loadInventory(final Inventory inventory, final UIConnectionResultCallback<Inventory> callback) {
		Inventory stock = new Inventory();
		stock.inventory = inventory.inventory;

		InventoryItem inventoryItem = new InventoryItem("coin_bundle_1", "$1.99", "coin_bundle_1", 2);
		stock.inventory.add(inventoryItem);

		inventoryItem = new InventoryItem("coin_bundle_2", "$2.99", "coin_bundle_2", 6);
		stock.inventory.add(inventoryItem);

		callback.onConnectionResult(stock);
	}

	@Override
	public void consumeOrders(List<Order> orders, Callback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setup(Callback callback) {
		callback.onSuccess("");
	}

	@Override
	public void purchaseCoins(InventoryItem inventoryItem, final UIConnectionResultCallback<List<Order>> callback) {
		List<Order> orders = new ArrayList<Order>();
		orders.add(inventoryItemToOrder(inventoryItem));
		callback.onConnectionResult(orders);
	}

	private Order inventoryItemToOrder(InventoryItem item) {
		Order order = new Order();

		order.orderId = "1";
		order.packageName = "com.xxx.galcon";
		order.purchaseTime = "3";
		order.purchaseState = "4";
		order.developerPayload = "5";
		order.productId = "android.test.purchased";
		order.token = "inapp:com.xxx.galcon:android.test.purchased";

		return order;
	}
}

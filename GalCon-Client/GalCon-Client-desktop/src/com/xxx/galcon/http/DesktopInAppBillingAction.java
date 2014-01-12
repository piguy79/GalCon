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
		callback.onSuccess("");
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

		order.orderId = "12999763169054705758.1373298375155582";
		order.packageName = "com.xxx.galcon";
		order.purchaseTime = "1389472914739";
		order.purchaseState = "0";
		order.developerPayload = "";
		order.productId = "coin_bundle_1";
		order.token = "mdtmbbjzbsfwrlpswfahdcab.AO-J1OxWst5eVCNffM81rISyZFnO1n2mlfrEyCyg2yoBXnisdE_bTdufGAQ-XoNh7uTGnZgDINH8N71A4pto6poxVeIdBUVaChGNVxmBnJa65fmvHqixqm8";

		return order;
	}
}

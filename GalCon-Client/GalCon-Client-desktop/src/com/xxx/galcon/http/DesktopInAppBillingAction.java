package com.xxx.galcon.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Order;

public class DesktopInAppBillingAction implements InAppBillingAction {

	public static Map<String, InventoryItem> storeItems = new HashMap<String, InventoryItem>();

	static {
		storeItems.put("coins_2", new InventoryItem("coins_2", "1.99", "Small coin bundle", 0));
		storeItems.put("coins_3", new InventoryItem("coins_3", "4.99", "Medium coin bundle", 0));
		storeItems.put("coins_4", new InventoryItem("coins_4", "9.99", "Large coin bundle", 0));
		storeItems.put("coins_5", new InventoryItem("coins_5", "19.99", "Huge coin bundle", 0));
	}

	@Override
	public void loadInventory(final Inventory inventory, final UIConnectionResultCallback<Inventory> callback) {

		Inventory mergedInventory = new Inventory();
		for (InventoryItem item : inventory.inventory) {
			InventoryItem storeItem = storeItems.get(item.sku);
			storeItem.numCoins = item.numCoins;

			mergedInventory.inventory.add(storeItem);
		}

		callback.onConnectionResult(mergedInventory);
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
		order.productId = item.sku;
		order.token = "mdtmbbjzbsfwrlpswfahdcab.AO-J1OxWst5eVCNffM81rISyZFnO1n2mlfrEyCyg2yoBXnisdE_bTdufGAQ-XoNh7uTGnZgDINH8N71A4pto6poxVeIdBUVaChGNVxmBnJa65fmvHqixqm8";

		return order;
	}
}

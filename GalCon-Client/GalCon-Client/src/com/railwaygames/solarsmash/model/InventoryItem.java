package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class InventoryItem extends JsonConvertible {

	public String sku;
	public String price;
	public String name;
	public int numCoins;

	/**
	 * Has the user purchased this item without the app yet consuming it?
	 */
	public Order unfulfilledOrder = null;

	public InventoryItem(String sku, String price, String name, int numCoins) {
		super();
		this.sku = sku;
		this.price = price;
		this.name = name;
		this.numCoins = numCoins;
	}

	public InventoryItem() {
	}

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.sku = jsonObject.getString("sku");
		this.numCoins = jsonObject.getInt("associatedCoins");
	}

	public boolean isAvailable() {
		return sku != null && price != null && name != null;
	}
}

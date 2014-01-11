package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConvertible;

public class Inventory extends JsonConvertible {

	public List<InventoryItem> inventory;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.inventory = new ArrayList<InventoryItem>();

		JSONArray array = jsonObject.getJSONArray("items");
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			InventoryItem item = new InventoryItem();
			item.consume(obj);
			inventory.add(item);
		}
	}

	public List<String> skus() {
		List<String> skus = new ArrayList<String>();
		for (InventoryItem item : inventory) {
			skus.add(item.sku);
		}

		return skus;
	}
}

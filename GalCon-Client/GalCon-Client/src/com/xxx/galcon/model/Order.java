package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConstructable;
import com.xxx.galcon.model.base.JsonConvertible;

public class Order extends JsonConvertible implements JsonConstructable{
	
	public String orderId;
	public String packageName;
	public String productId;
	public String purchaseTime;
	public String purchaseState;
	public String developerPayload;
	public String token;
	public int associatedCoins;
	
	public Order(){}
	
	public Order(String jsonPurchaseInfo, int associatedCoins){
	        JSONObject o;
			try {
				o = new JSONObject(jsonPurchaseInfo);
				orderId = o.optString("orderId");
		        packageName = o.optString("packageName");
		        productId = o.optString("productId");
		        purchaseTime = o.optString("purchaseTime");
		        purchaseState = o.optString("purchaseState");
		        developerPayload = o.optString("developerPayload");
		        token = o.optString("token", o.optString("purchaseToken"));
		        this.associatedCoins = associatedCoins;
			} catch (JSONException e) {
				e.printStackTrace();
			}
	        
	}
	
	public Order(String orderId, String packageName, String productId, String purchaseTime, String purchaseState, String developerPayload, String token, int associatedCoins){
		this.orderId = orderId;
		this.packageName = packageName;
		this.productId = productId;
		this.purchaseTime = purchaseTime;
		this.purchaseState = purchaseState;
		this.developerPayload = developerPayload;
		this.token = token;
		this.associatedCoins = associatedCoins;
	}
	
	
	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.orderId = jsonObject.getString("orderId");
		this.packageName = jsonObject.getString("packageName");
		this.purchaseTime = jsonObject.getString("purchaseTime");
		this.purchaseState = jsonObject.getString("purchaseState");
		this.developerPayload = jsonObject.getString("developerPayload");
		this.token = jsonObject.getString("token");
		this.associatedCoins = jsonObject.getInt("associatedCoins");
		
	}

	@Override
	public JSONObject asJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("orderId", orderId);
		jsonObject.put("packageName", packageName);
		jsonObject.put("purchaseTime", purchaseTime);
		jsonObject.put("purchaseState", purchaseState);
		jsonObject.put("developerPayload", developerPayload);
		jsonObject.put("token", token);
		jsonObject.put("associatedCoins", associatedCoins);
		
		return jsonObject;
	}

}

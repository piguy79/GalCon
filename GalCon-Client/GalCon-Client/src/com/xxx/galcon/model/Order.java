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
	
	public Order(){}
	
	public Order(String jsonPurchaseInfo){
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
			} catch (JSONException e) {
				e.printStackTrace();
			}
	        
	}
	
	
	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.orderId = jsonObject.getString("orderId");
		this.packageName = jsonObject.getString("packageName");
		this.purchaseTime = jsonObject.getString("purchaseTime");
		this.purchaseState = jsonObject.getString("purchaseState");
		this.developerPayload = jsonObject.getString("developerPayload");
		this.token = jsonObject.getString("token");
		
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
		
		return jsonObject;
	}

}

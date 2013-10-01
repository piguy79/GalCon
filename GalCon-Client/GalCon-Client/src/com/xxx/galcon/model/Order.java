package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConvertible;

public class Order extends JsonConvertible{
	
	public String orderId;
	public String packageName;
	public String productId;
	public String purchaseTime;
	public String purchaseState;
	public String developerPayload;
	public String token;
	
	
	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.orderId = jsonObject.getString("orderId");
		this.packageName = jsonObject.getString("packageName");
		this.purchaseTime = jsonObject.getString("purchaseTime");
		this.purchaseState = jsonObject.getString("purchaseState");
		this.developerPayload = jsonObject.getString("developerPayload");
		this.token = jsonObject.getString("token");
		
	}

}

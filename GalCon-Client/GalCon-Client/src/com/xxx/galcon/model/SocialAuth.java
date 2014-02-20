package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConvertible;

public class SocialAuth extends JsonConvertible {
	
	public String id;
	public String authProvider;
	
	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.id = jsonObject.getString("id");
		this.authProvider = jsonObject.getString("provider");
		
	}

}

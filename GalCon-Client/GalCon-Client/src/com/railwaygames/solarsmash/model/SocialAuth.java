package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class SocialAuth extends JsonConvertible {
	
	public String id;
	public String authProvider;
	
	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.id = jsonObject.getString("id");
		this.authProvider = jsonObject.getString("provider");
		
	}

}

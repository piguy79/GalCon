package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class BaseResult extends JsonConvertible {
	
	public boolean success;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		success = jsonObject.getBoolean("success");
		
	}

}

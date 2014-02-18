package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConvertible;

public class BaseResult extends JsonConvertible {
	
	public boolean success;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		success = jsonObject.getBoolean("success");
		
	}

}

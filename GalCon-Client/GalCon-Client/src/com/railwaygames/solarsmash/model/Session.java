package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class Session extends JsonConvertible {

	public String session;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.errorMessage = jsonObject.optString("error");
		this.session = jsonObject.optString("session");
	}
}

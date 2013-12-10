package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConvertible;

public class Session extends JsonConvertible {

	public String session;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.errorMessage = jsonObject.optString("error");
		this.session = jsonObject.optString("session");
	}
}

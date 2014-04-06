package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class Social extends JsonConvertible {
	
	public String invitee;
	public String status;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		invitee = jsonObject.optString("invitee");
		status = jsonObject.optString("status");
	}

}

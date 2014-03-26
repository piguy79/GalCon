package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConvertible;

public class Social extends JsonConvertible {
	
	public String invitee;
	public String status;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		invitee = jsonObject.optString("invitee");
		status = jsonObject.optString("status");
	}

}

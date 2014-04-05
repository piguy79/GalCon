package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class GameCount extends JsonConvertible {
	public int pendingGameCount = 0;
	public int inviteCount = 0;
	

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		pendingGameCount = jsonObject.optInt("c");
		inviteCount = jsonObject.optInt("i");
	}
}

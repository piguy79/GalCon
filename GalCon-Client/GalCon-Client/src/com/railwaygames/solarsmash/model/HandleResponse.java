package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class HandleResponse extends JsonConvertible {

	public boolean handleCreated;
	public Player player;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		handleCreated = jsonObject.optBoolean(Constants.CREATED, false);
		if (handleCreated) {
			player = new Player();
			player.consume(jsonObject.getJSONObject(Constants.PLAYER));
		}
	}
}

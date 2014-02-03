package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

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

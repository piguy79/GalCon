package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class HandleResponse implements JsonConvertible {

	public boolean handleCreated;
	public Player player;

	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		handleCreated = jsonObject.getBoolean(Constants.CREATED);
		if (handleCreated) {
			player = new Player();
			player.consume(jsonObject.getJSONObject(Constants.PLAYER));
		}
	}
}

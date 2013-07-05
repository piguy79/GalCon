package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class HandleResponse extends JsonConvertible {

	public boolean handleCreated;
	public String reason;
	public Player player;

	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		handleCreated = jsonObject.getBoolean(Constants.CREATED);
		reason = jsonObject.optString(Constants.REASON);
		if (handleCreated) {
			player = new Player();
			player.consume(jsonObject.getJSONObject(Constants.PLAYER));
		}
	}
}

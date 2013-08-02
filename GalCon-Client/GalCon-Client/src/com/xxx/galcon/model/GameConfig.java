package com.xxx.galcon.model;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConvertible;

public class GameConfig extends JsonConvertible {

	private java.util.Map<String, String> config = new HashMap<String, String>();

	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null) {
			return;
		}

		JSONArray values = jsonObject.optJSONArray("values");
		if (values == null) {
			return;
		}

		for (int i = 0; i < values.length(); ++i) {
			JSONObject configValue = values.getJSONObject(i);
			config.put(configValue.getString("key"), configValue.getString("value"));
		}
	}
}

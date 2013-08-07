package com.xxx.galcon.model;

import java.util.HashMap;
import java.util.Iterator;

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

		JSONObject values = jsonObject.optJSONObject("values");
		if (values == null) {
			return;
		}

		for (Iterator iter = values.keys(); iter.hasNext();) {
			String key = (String) iter.next();
			config.put(key, values.getString(key));
		}
	}

	public String getValue(String key) {
		return config.get(key);
	}
}

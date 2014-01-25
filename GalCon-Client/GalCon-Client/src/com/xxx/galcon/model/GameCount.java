package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConvertible;

public class GameCount extends JsonConvertible {
	public int count = 0;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		count = jsonObject.optInt("c");
	}
}

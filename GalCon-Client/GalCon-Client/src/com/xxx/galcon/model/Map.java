package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class Map extends JsonConvertible {

	public int key;
	public int availableFromLevel;
	public String title;
	public String description;

	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		key = jsonObject.getInt(Constants.KEY);
		availableFromLevel = jsonObject.getInt(Constants.AVAILABLE_FROM_LEVEL);
		title = jsonObject.getString(Constants.TITLE);
		description = jsonObject.getString(Constants.DESCRIPTION);
	}

}

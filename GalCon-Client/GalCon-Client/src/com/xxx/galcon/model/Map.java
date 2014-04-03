package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class Map extends JsonConvertible {

	public int key;
	public int availableFromXp;
	public String title;
	public String description;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		key = jsonObject.getInt(Constants.KEY);
		availableFromXp = jsonObject.getInt(Constants.AVAILABLE_FROM_XP);
		title = jsonObject.getString(Constants.TITLE);
		description = jsonObject.getString(Constants.DESCRIPTION);
	}

}

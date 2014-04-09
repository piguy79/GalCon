package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class Map extends JsonConvertible {

	public int key;
	public int availableFromXp;
	public String title;
	public String description;
	public boolean canHarvest;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		key = jsonObject.getInt(Constants.KEY);
		availableFromXp = jsonObject.getInt(Constants.AVAILABLE_FROM_XP);
		title = jsonObject.getString(Constants.TITLE);
		description = jsonObject.getString(Constants.DESCRIPTION);
		canHarvest = jsonObject.optBoolean("canHarvest", false);
	}

}

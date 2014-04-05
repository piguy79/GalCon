package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class Rank extends JsonConvertible {
	
	public Long level;
	public int startFrom;
	public int endAt;
	
	
	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.level =jsonObject.getLong(Constants.LEVEL);
		this.startFrom  = Integer.parseInt(jsonObject.getString(Constants.START_FROM));
		this.endAt  = Integer.parseInt(jsonObject.getString(Constants.END_AT));
		
	}

}

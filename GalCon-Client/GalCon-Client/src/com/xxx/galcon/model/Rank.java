package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

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

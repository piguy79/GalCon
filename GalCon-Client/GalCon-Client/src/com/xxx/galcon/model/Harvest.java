package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConvertible;

public class Harvest extends JsonConvertible {

	public String status;
	public int startingRound;
	public int saveRound;
	public static final String ACTIVE = "ACTIVE";
	
	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.status  = jsonObject.optString("status");
		this.startingRound = jsonObject.optInt("startingRound");
		this.saveRound = jsonObject.optInt("saveRound");
	}

	public boolean isActive() {
		return this.status != null && this.status.equals(ACTIVE);
	}
	
}

package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConvertible;

public class BattleStats extends JsonConvertible {

	public int previousShipsOnPlanet;
	public String previousPlanetOwner;
	public String newPlanetOwner;
	public double defenceStrength;
	public double attackStrength;
	public double defenceMultiplier;
	public boolean conquer;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		if (jsonObject != null) {
			this.previousShipsOnPlanet = jsonObject.optInt("previousShipsOnPlanet");
			this.previousPlanetOwner = jsonObject.optString("previousPlanetOwner");
			this.newPlanetOwner = jsonObject.optString("newPlanetOwner");
			this.defenceStrength = jsonObject.optDouble("defenceStrength");
			this.attackStrength = jsonObject.optDouble("attackStrength");
			this.defenceMultiplier = jsonObject.optDouble("defenceMultiplier");
			this.conquer = jsonObject.optBoolean("conquer");
		}

	}
}

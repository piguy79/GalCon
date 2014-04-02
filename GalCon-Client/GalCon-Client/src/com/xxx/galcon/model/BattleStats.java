package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.base.JsonConvertible;

public class BattleStats extends JsonConvertible {

	public int previousShipsOnPlanet;
	public String previousPlanetOwner;
	public double attackMultiplier;
	public double defenceMultiplier;
	public Boolean diedInAirAttack;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		if (jsonObject != null) {
			this.previousShipsOnPlanet = jsonObject.optInt("previousShipsOnPlanet");
			this.previousPlanetOwner = jsonObject.optString("previousPlanetOwner");
			this.attackMultiplier = jsonObject.optDouble("atckMult");
			this.defenceMultiplier = jsonObject.optDouble("defMult");
			this.diedInAirAttack = jsonObject.optBoolean("diaa");
		}
	}
}

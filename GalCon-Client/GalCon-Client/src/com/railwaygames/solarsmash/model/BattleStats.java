package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class BattleStats extends JsonConvertible {

	public int previousShipsOnPlanet;
	public String previousPlanetOwner;
	public double attackMultiplier;
	public double defenceMultiplier;
	public Boolean diedInAirAttack;
	public int startFleet;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		if (jsonObject != null) {
			this.previousShipsOnPlanet = jsonObject.optInt("prevShipsOnPlanet");
			this.previousPlanetOwner = jsonObject.optString("prevPlanetOwner");
			this.attackMultiplier = jsonObject.optDouble("atckMult");
			this.defenceMultiplier = jsonObject.optDouble("defMult");
			this.diedInAirAttack = jsonObject.optBoolean("diaa");
			this.startFleet = jsonObject.optInt("startFleet");
		}
	}
}

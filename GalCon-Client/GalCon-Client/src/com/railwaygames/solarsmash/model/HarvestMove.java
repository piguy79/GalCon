package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConstructable;

public class HarvestMove implements JsonConstructable{
	
	public String planet;
	
	public HarvestMove(String planetName){
		this.planet = planetName;
	}

	@Override
	public JSONObject asJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("planet", planet);
		return jsonObject;
	}

}

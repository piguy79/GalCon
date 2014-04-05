package com.railwaygames.solarsmash.model.base;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonConstructable {
	
	JSONObject asJson() throws JSONException;

}

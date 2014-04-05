package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class Maps extends JsonConvertible {

	public List<Map> allMaps = new ArrayList<Map>();

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		JSONArray maps = jsonObject.optJSONArray(Constants.ITEMS);

		if (maps != null) {
			for (int i = 0; i < maps.length(); ++i) {
				Map map = new Map();
				map.consume(maps.getJSONObject(i));
				allMaps.add(map);
			}
		}
	}
	
	public Map getMap(int mapKey){
		for(Map map : allMaps){
			if(map.key == mapKey){
				return map;
			}
		}
		
		return null;
	}
}

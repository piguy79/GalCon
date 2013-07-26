package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class Maps extends JsonConvertible {

	public List<Map> allMaps = new ArrayList<Map>();

	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		JSONArray maps = jsonObject.optJSONArray(Constants.ITEMS);

		if (maps != null) {
			for (int i = 0; i < maps.length(); ++i) {
				Map map = new Map();
				map.consume(maps.getJSONObject(i));
				allMaps.add(map);
			}
		}
	}
}

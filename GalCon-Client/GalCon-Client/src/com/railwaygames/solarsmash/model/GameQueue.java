package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class GameQueue extends JsonConvertible {
	
	public List<GameQueueItem> gameQueueItems = new ArrayList<GameQueueItem>();
	
	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		JSONArray queueReturn = jsonObject.optJSONArray(Constants.ITEMS);
		
		if(queueReturn != null){
			for(int i = 0; i < queueReturn.length(); i++){
				JSONObject itemJson = queueReturn.getJSONObject(i);
				GameQueueItem item = new GameQueueItem();
				item.consume(itemJson);
				gameQueueItems.add(item);
			}
		}
	}

}

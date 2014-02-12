package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

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

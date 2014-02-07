package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.MinifiedGame.MinifiedPlayer;
import com.xxx.galcon.model.base.JsonConvertible;

public class GameQueue extends JsonConvertible {
	
	public MinifiedPlayer requester;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		JSONObject requesterJson = jsonObject.getJSONObject("requester");
		requester = new MinifiedPlayer();
		requester.handle = requesterJson.getString("handle");
		requester.rank = requesterJson.getInt("rank");
		
	}

}

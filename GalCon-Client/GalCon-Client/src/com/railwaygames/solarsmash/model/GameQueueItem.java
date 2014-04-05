package com.railwaygames.solarsmash.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.MinifiedGame.MinifiedPlayer;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class GameQueueItem extends JsonConvertible {
	
	public MinifiedPlayer requester;
	public String inviteeHandle;
	public MinifiedGame game;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		JSONObject requesterJson = jsonObject.getJSONObject("requester");
		requester = new MinifiedPlayer();
		requester.handle = requesterJson.getString("handle");
		requester.xp = requesterJson.getInt("xp");
		
		inviteeHandle = jsonObject.getString("inviteeHandle");
		
		JSONObject minfiedGameJson = jsonObject.getJSONObject("minifiedGame");
		game = new MinifiedGame();
		game.consume(minfiedGameJson);
		
	}

}

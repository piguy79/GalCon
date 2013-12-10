/**
 * 
 */
package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

/**
 * Information regarding the current round being played. Such as which players turn it is
 * and the round number.
 * 
 * @author conormullen
 *
 */
public class RoundInformation extends JsonConvertible {
	
	public int currentRound;
	public List<String> players = new ArrayList<String>();
	
	
	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.currentRound = jsonObject.getInt(Constants.ROUND_NUMBER);
				
		players = new ArrayList<String>();
		JSONArray playersJson = jsonObject.optJSONArray(Constants.PLAYERS_WHO_MOVED);
		if (playersJson != null) {
			for (int i = 0; i < playersJson.length(); i++) {
				String player = playersJson.getString(i);
				this.players.add(player);
			}
		}
		
	}

}

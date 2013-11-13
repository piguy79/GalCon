package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class MinifiedGame extends JsonConvertible {
	
	public String id;
	public Date createdDate;
	public List<String> players;
	public boolean moveAvailable;
	public String winner;
	public Date winningDate = null;
	
	
	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.id = jsonObject.getString("id");
		this.createdDate = formatDate(jsonObject, "createdDate");
		this.players = new ArrayList<String>();
		
		JSONArray playersJson = jsonObject.optJSONArray(Constants.PLAYERS);
		if (playersJson != null) {
			for (int i = 0; i < playersJson.length(); i++) {
				String player = playersJson.getString(i);
				this.players.add(player);
			}
		}
		
		this.moveAvailable = jsonObject.getBoolean("moveAvailable");
		this.winner = jsonObject.optString("winner");
		this.winningDate = formatDate(jsonObject, "winningDate");
		
	}
	
	public boolean hasWinner(){
		return winner != null && winner.length() > 0;
	}
	
	public List<String> allPlayersExcept(String playerHandleToExclude) {
		List<String> otherPlayers = new ArrayList<String>();

		for (String player : this.players) {
			if (!player.equals(playerHandleToExclude)) {
				otherPlayers.add(player);
			}
		}

		return otherPlayers;
	}

}

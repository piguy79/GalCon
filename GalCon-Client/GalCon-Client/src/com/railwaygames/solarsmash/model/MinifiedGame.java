package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class MinifiedGame extends JsonConvertible {

	public String id;
	public Date createdDate;
	public List<MinifiedPlayer> players;
	public boolean moveAvailable;
	public String winner;
	public Date winningDate = null;
	public int mapKey;
	public Social social;

	public static class MinifiedPlayer {
		public Auth auth;
		public String handle;
		public int xp;
	}

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.id = jsonObject.getString("id");
		this.createdDate = formatDate(jsonObject, "createdDate");
		this.players = new ArrayList<MinifiedPlayer>();

		JSONArray playersJson = jsonObject.optJSONArray(Constants.PLAYERS);
		if (playersJson != null) {
			for (int i = 0; i < playersJson.length(); i++) {
				JSONObject player = playersJson.getJSONObject(i);
				JSONObject authJson = player.getJSONObject("auth");
				
				MinifiedPlayer minifiedPlayer = new MinifiedPlayer();
				minifiedPlayer.auth = new Auth();
				minifiedPlayer.auth.consume(authJson);
				minifiedPlayer.handle = player.getString("handle");
				minifiedPlayer.xp = player.getInt("xp");
				this.players.add(minifiedPlayer);
			}
		}

		this.moveAvailable = jsonObject.getBoolean("moveAvailable");
		this.winner = jsonObject.optString("winner");
		this.winningDate = formatDate(jsonObject, "date");
		this.mapKey = jsonObject.getInt("map");
		if(jsonObject.has("social")){
			this.social = new Social();
			this.social.consume(jsonObject.getJSONObject("social"));
		}
	}

	public boolean hasWinner() {
		return winner != null && winner.length() > 0;
	}

	public List<MinifiedPlayer> allPlayersExcept(String playerHandleToExclude) {
		List<MinifiedPlayer> otherPlayers = new ArrayList<MinifiedPlayer>();

		for (MinifiedPlayer player : this.players) {
			if (!player.handle.equals(playerHandleToExclude)) {
				otherPlayers.add(player);
			}
		}

		return otherPlayers;
	}

	public boolean hasBeenDeclined() {
		return social != null && social.status.equals("DECLINED");
	}

}

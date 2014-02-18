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
	public List<MinifiedPlayer> players;
	public boolean moveAvailable;
	public String winner;
	public Date winningDate = null;
	public int mapKey;
	public String social;

	public static class MinifiedPlayer {
		public String handle;
		public int rank;
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
				MinifiedPlayer minifiedPlayer = new MinifiedPlayer();
				minifiedPlayer.handle = player.getString("handle");
				minifiedPlayer.rank = player.getInt("rank");
				this.players.add(minifiedPlayer);
			}
		}

		this.moveAvailable = jsonObject.getBoolean("moveAvailable");
		this.winner = jsonObject.optString("winner");
		this.winningDate = formatDate(jsonObject, "winningDate");
		this.mapKey = jsonObject.getInt("map");
		this.social = jsonObject.optString("social");
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

}

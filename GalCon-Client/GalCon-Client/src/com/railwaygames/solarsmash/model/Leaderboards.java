package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class Leaderboards extends JsonConvertible {

	public java.util.Map<String, List<LeaderboardEntry>> leaderboards = new HashMap<String, List<LeaderboardEntry>>();

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		JSONArray boards = jsonObject.optJSONArray("boards");
		if (boards != null && boards.length() > 0) {
			for (int i = 0; i < boards.length(); ++i) {
				JSONArray entriesForBoard = boards.getJSONArray(i);
				if (entriesForBoard != null && entriesForBoard.length() > 0) {
					List<LeaderboardEntry> entries = new ArrayList<LeaderboardEntry>();
					for (int j = 0; j < entriesForBoard.length(); ++j) {
						JSONObject entry = entriesForBoard.getJSONObject(j);
						LeaderboardEntry lb = new LeaderboardEntry();
						lb.id = entry.getString("id");
						lb.score = (float) entry.getDouble("score");

						JSONObject user = entry.getJSONObject("user");
						lb.handle = user.getString("handle");
						lb.userId = user.getString("id");

						JSONObject record = entry.getJSONObject("record");
						lb.wins = record.getInt("w");
						lb.losses = record.getInt("l");

						entries.add(lb);
					}
					leaderboards.put(entries.get(0).id, entries);
				}
			}
		}
	}

	public static class LeaderboardEntry {
		public float score;
		public String handle;
		public String userId;
		public String id;
		public int wins;
		public int losses;
	}
}

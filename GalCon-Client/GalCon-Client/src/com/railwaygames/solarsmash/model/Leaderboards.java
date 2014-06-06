package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class Leaderboards extends JsonConvertible {

	public List<Leaderboard> leaderboards = new ArrayList<Leaderboard>();

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		JSONArray boards = jsonObject.optJSONArray("boards");
		if (boards != null && boards.length() > 0) {
			for (int i = 0; i < boards.length(); ++i) {
				JSONObject board = boards.getJSONObject(i);

				Leaderboard lb = new Leaderboard();
				lb.id = board.getString("id");
				lb.score = (float) board.getDouble("score");
				
				JSONObject user = board.getJSONObject("user");
				lb.handle = user.getString("handle");
				lb.userId = user.getString("id");
				
				JSONObject record = board.getJSONObject("record");
				lb.wins = record.getInt("w");
				lb.losses = record.getInt("l");

				leaderboards.add(lb);
			}
		}
	}

	public static class Leaderboard {
		public float score;
		public String handle;
		public String userId;
		public String id;
		public int wins;
		public int losses;
	}
}

package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class EndGameInformation extends JsonConvertible {

	public String winnerHandle;
	public List<String> loserHandles = new ArrayList<String>();
	public DateTime date = null;
	public int xp;
	public boolean draw;
	public String decline;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.winnerHandle = jsonObject.optString(Constants.WINNER_HANDLE);
		this.date = formatDate(jsonObject, Constants.DATE);
		this.draw = jsonObject.optBoolean(Constants.DRAW);
		this.xp = jsonObject.getInt(Constants.XP);
		this.decline = jsonObject.optString("decline");

		this.loserHandles = new ArrayList<String>();
		JSONArray losersJson = jsonObject.optJSONArray(Constants.LOSER_HANDLES);
		if (losersJson != null) {
			for (int i = 0; i < losersJson.length(); i++) {
				String player = losersJson.getString(i);
				this.loserHandles.add(player);
			}
		}
	}
}

package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class EndGameInformation extends JsonConvertible {

	public String winnerHandle;
	public List<String> loserHandles = new ArrayList<String>();
	public Date winningDate = null;
	public int xpAwardToWinner;
	public boolean draw;

	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.winnerHandle = jsonObject.optString(Constants.WINNER_HANDLE);
		this.winningDate = formatDate(jsonObject, Constants.WINNING_DATE);
		this.draw = jsonObject.optBoolean(Constants.DRAW);
		this.xpAwardToWinner = jsonObject.getInt(Constants.XP_AWARD_TO_WINNER);

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

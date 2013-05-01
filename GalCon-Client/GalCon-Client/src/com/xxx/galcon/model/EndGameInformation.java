package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class EndGameInformation extends JsonConvertible{
	
	public String winner;
	public List<String> losers = new ArrayList<String>();
	public Date winningDate = null;
	public boolean draw;
	
	
	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.winner = jsonObject.getString(Constants.WINNER);
		this.winningDate = formatDate(jsonObject, Constants.WINNING_DATE);
		this.draw = jsonObject.getBoolean(Constants.DRAW);
		
		this.losers = new ArrayList<String>();
		JSONArray losersJson = jsonObject.getJSONArray(Constants.LOSERS);
		for (int i = 0; i < losersJson.length(); i++) {
			String player = losersJson.getString(i);
			this.losers.add(player);
		}
	}

}

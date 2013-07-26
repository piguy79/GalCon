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
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.base.JsonConvertible;

/**
 * Class representing a Player.
 * 
 * 
 * 
 * @author conormullen
 *
 */
public class Player extends JsonConvertible{
	
	public String name;
	public String handle;
	public Integer xp;
	public List<String> currentGames;
	public Rank rank;
	public Integer coins;
	public Long usedCoins;
	
	
	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.name = jsonObject.getString(Constants.NAME);
		this.handle = jsonObject.optString(Constants.HANDLE);
		this.xp  = jsonObject.getInt(Constants.XP);
		this.coins = jsonObject.getInt("coins");
		this.usedCoins = jsonObject.optLong("usedCoins");
		
		JSONObject rankInfo = jsonObject.getJSONObject(Constants.RANK_INFO);
		this.rank = new Rank();
		rank.consume(rankInfo);
		
		this.currentGames = new ArrayList<String>();
		
		JSONArray currentGamesJson = jsonObject.getJSONArray(Constants.CURRENT_GAMES);
		for (int i = 0; i < currentGamesJson.length(); i++) {
			String game = currentGamesJson.getString(i);
			this.currentGames.add(game);
		}
		
	}


	public boolean hasMoved(GameBoard gameBoard) {
		return gameBoard.roundInformation.players.contains(handle);
	}
	
	

}

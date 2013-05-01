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
	public Integer xp;
	public List<String> currentGames;
	public Rank rank;
	
	
	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.name = jsonObject.getString(Constants.NAME);
		this.xp  = Integer.parseInt(jsonObject.getString(Constants.XP));
		
		JSONObject rankInfo = jsonObject.getJSONObject(Constants.RANK_INFO);
		this.rank = new Rank();
		this.rank.level = rankInfo.getInt(Constants.LEVEL);
		this.rank.startFrom = rankInfo.getInt(Constants.START_FROM);
		this.rank.endAt = rankInfo.getInt(Constants.END_AT);
		
		this.currentGames = new ArrayList<String>();
		
		JSONArray currentGamesJson = jsonObject.getJSONArray(Constants.CURRENT_GAMES);
		for (int i = 0; i < currentGamesJson.length(); i++) {
			String game = currentGamesJson.getString(i);
			this.currentGames.add(game);
		}
		
	}
	
	public boolean isCurrentPlayerForGame(GameBoard gameBoard){
		return gameBoard.currentPlayerToMove.equals(name);
	}
	

}

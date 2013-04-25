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
public class Player implements JsonConvertible{
	
	public String name;
	public Integer xp;
	public String rank;
	public List<String> currentGames;
	
	
	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.name = jsonObject.getString(Constants.NAME);
		this.rank  = jsonObject.getString(Constants.RANK);
		this.xp  = Integer.parseInt(jsonObject.getString(Constants.XP));
		
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

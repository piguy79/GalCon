/**
 * 
 */
package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.config.ConfigConstants;
import com.xxx.galcon.config.ConfigResolver;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.model.base.JsonConvertible;

/**
 * Class representing a Player.
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
	
	public Long timeSinceCoinsHaveBeenUsed(){
		return  new DateTime(DateTimeZone.UTC).getMillis() - usedCoins;
	}
	
	public Long timeRemainingForNewcoins(){
		return timeLapse() - timeSinceCoinsHaveBeenUsed();
	}
	
	public Long timeLapse(){
		return Long.parseLong(ConfigResolver.getByConfigKey(ConfigConstants.TIME_LAPSE_FOR_NEW_COINS));
	}
	
	public DateTime timeRemainingUntilCoinsAvailable(){
		
		if (usedCoins != null && usedCoins != -1L) {

			Long timeSinceUsedCoins = timeSinceCoinsHaveBeenUsed();

			if (timeSinceUsedCoins < timeLapse()) {
				return new DateTime(timeLapse() - timeSinceUsedCoins);
			}
		} 
		
		return null;
	}
	
	

}

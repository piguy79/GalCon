/**
 * 
 */
package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.config.ConfigConstants;
import com.xxx.galcon.config.ConfigResolver;
import com.xxx.galcon.model.base.JsonConvertible;

/**
 * Class representing a Player.
 * 
 * @author conormullen
 * 
 */
public class Player extends JsonConvertible {
	public Auth auth;
	public String handle;
	public Integer xp;
	public Integer coins;
	public Long usedCoins;
	public boolean watchedAd;
	public List<Order> consumedOrders;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {

		JSONObject authObject = jsonObject.getJSONObject("auth");
		this.auth = new Auth();
		auth.consume(authObject);
		this.handle = jsonObject.optString(Constants.HANDLE);
		this.xp = jsonObject.getInt(Constants.XP);
		this.coins = jsonObject.getInt(Constants.COINS);
		this.usedCoins = jsonObject.optLong(Constants.USED_COINS);
		this.watchedAd = jsonObject.getBoolean(Constants.WATCHED_AD);

		this.consumedOrders = new ArrayList<Order>();
		JSONArray consumedOrders = jsonObject.getJSONArray("consumedOrders");
		for (int i = 0; i < consumedOrders.length(); i++) {
			JSONObject orderObject = consumedOrders.getJSONObject(i);
			Order order = new Order();
			order.consume(orderObject);
			this.consumedOrders.add(order);
		}
	}

	public boolean hasCoinInformation() {
		return usedCoins != null && coins != null;
	}
	
	public void addAuthProvider(String authProvider, String id){
		if(auth == null){
			auth = new Auth();
			auth.auth = new HashMap<String, String>();
		}else if(auth.auth == null){
			auth.auth = new HashMap<String, String>();
		}
		
		auth.auth.put(authProvider, id);
	}

	public boolean hasMoved(GameBoard gameBoard) {
		return gameBoard.roundInformation.players.contains(handle);
	}

	private Long timeSinceCoinsHaveBeenUsed() {
		return new DateTime(DateTimeZone.UTC).getMillis() - usedCoins;
	}

	private Long timeLapse() {
		return Long.parseLong(ConfigResolver.getByConfigKey(ConfigConstants.TIME_LAPSE_FOR_NEW_COINS));
	}

	public DateTime timeRemainingUntilCoinsAvailable() {
		if (usedCoins != null && usedCoins != -1L) {
			Long timeSinceUsedCoins = timeSinceCoinsHaveBeenUsed();

			if (timeSinceUsedCoins < timeLapse()) {
				return new DateTime(timeLapse() - timeSinceUsedCoins);
			}
		}

		return null;
	}

	public boolean hasSpeedIncrease(GameBoard gameBoard) {
		for(Planet planet : gameBoard.planets){
			if(planet.isOwnedBy(handle) && planet.hasAbility() && planet.ability.equals(Constants.ABILITY_SPEED)){
				return true;
			}
		}
		return false;
	}
}

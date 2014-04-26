/**
 * 
 */
package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

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
	public List<Order> consumedOrders;

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {

		if (jsonObject.has("auth")) {
			JSONObject authObject = jsonObject.getJSONObject("auth");
			this.auth = new Auth();
			auth.consume(authObject);
		}

		this.handle = jsonObject.optString(Constants.HANDLE);
		this.xp = jsonObject.getInt(Constants.XP);
		this.coins = jsonObject.optInt(Constants.COINS);

		if (jsonObject.has("consumedOrders")) {
			this.consumedOrders = new ArrayList<Order>();
			JSONArray consumedOrders = jsonObject.getJSONArray("consumedOrders");
			for (int i = 0; i < consumedOrders.length(); i++) {
				JSONObject orderObject = consumedOrders.getJSONObject(i);
				Order order = new Order();
				order.consume(orderObject);
				this.consumedOrders.add(order);
			}
		}

	}

	public void addAuthProvider(String authProvider, String id) {
		if (auth == null) {
			auth = new Auth();
			auth.auth = new HashMap<String, String>();
		} else if (auth.auth == null) {
			auth.auth = new HashMap<String, String>();
		}

		auth.auth.put(authProvider, id);
	}

	public boolean hasMoved(GameBoard gameBoard) {
		return gameBoard.roundInformation.players.contains(handle);
	}

	private boolean hasAbility(String ability, GameBoard gameBoard) {
		for (Planet planet : gameBoard.planets) {
			if (planet.isOwnedBy(handle) && planet.hasAbility() && planet.ability.equals(ability)) {
				return true;
			}
		}
		return false;
	}

	public float abilityIncreaseToApply(String ability, GameBoard gameBoard) {
		float abilityIncreaseToApply = 0.0f;
		float harvestIncreaseToApply = 0.0f;

		if (hasAbility(ability, gameBoard)) {
			harvestIncreaseToApply = findHarvestIncrease(ability, gameBoard);
			abilityIncreaseToApply = new Float(gameBoard.gameConfig.getValue(Constants.ABILITY_SPEED))
					* abilityPlanetsOwned(ability, gameBoard);
		}

		return abilityIncreaseToApply + harvestIncreaseToApply;

	}

	private float findHarvestIncrease(String ability, GameBoard gameBoard) {
		float harvestEnhance = 0.0f;
		for (Planet planet : gameBoard.planets) {
			if (planet.isOwnedBy(handle) && planet.hasAbility() && planet.ability.equals(ability)
					&& planet.isUnderHarvest()) {
				harvestEnhance += new Float(gameBoard.gameConfig.getValue(Constants.HARVEST_ENHANCMENT));
			}
		}
		return harvestEnhance;
	}

	private int abilityPlanetsOwned(String ability, GameBoard gameBoard) {
		int count = 0;
		for (Planet planet : gameBoard.planets) {
			if (planet.isOwnedBy(handle) && planet.hasAbility() && planet.ability.equals(ability)) {
				count++;
			}
		}

		return count;
	}
}

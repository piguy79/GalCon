/**
 * 
 */
package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConvertible;

/**
 * Class representing a Player.
 */
public class PlayerList extends JsonConvertible {
	public List<Player> players = new ArrayList<Player>();

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {

		if (jsonObject.has("options")) {
			JSONArray array = jsonObject.getJSONArray("options");
			for (int i = 0; i < array.length(); i++) {
				Player player = new Player();
				player.consume(array.getJSONObject(i));
				players.add(player);
			}
		} else {
			Player player = new Player();
			player.consume(jsonObject);
			players.add(player);
		}
	}
}

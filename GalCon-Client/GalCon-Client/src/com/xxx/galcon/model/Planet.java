package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.base.JsonConvertible;

public class Planet extends JsonConvertible {
	public String owner = Constants.OWNER_NO_ONE;
	public float shipRegenRate = 1.0f;
	public int numberOfShips;
	public String name;
	public String id;
	public Point position;
	public boolean touched = false;
	public String ability;

	public Planet() {

	}

	@Override
	public void consume(JSONObject jsonObject) {
		try {
			this.name = jsonObject.getString(Constants.NAME);
			this.shipRegenRate = (float) jsonObject.getDouble(Constants.SHIP_REGEN_RATE);
			this.numberOfShips = jsonObject.getInt(Constants.NUMBER_OF_SHIPS);
			this.ability = jsonObject.getString(Constants.ABILITY);
			if (jsonObject.has(Constants.OWNER_HANDLE)) {
				this.owner = jsonObject.getString(Constants.OWNER_HANDLE);
			}
			JSONObject positionJson = jsonObject.getJSONObject(Constants.POSITION);
			Point position = new Point();
			position.consume(positionJson);
			this.position = position;
			this.id = jsonObject.getString(Constants.ID);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public boolean isOwnedBy(Player player) {
		return owner.equals(player.handle);
	}

	public boolean hasAbility() {
		return ability != null && !ability.isEmpty();
	}

	public String getAbilityDescription() {
		return Constants.PLANET_ABILITIES.get(ability);
	}

}

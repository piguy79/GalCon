package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class Planet implements JsonConvertible{
	public int tileNumberX = 0, tileNumberY = 0;
	public String owner = Constants.OWNER_NO_ONE;
	public float shipRegenRate = 1.0f;
	public int numberOfShips;
	public String name;
	public String id;
	public PlanetPosition position;
	public boolean touched = false;
	
	public Planet(){
		
	}

	public Planet(int tileNumberX, int tileNumberY, float shipRegenRate) {
		super();
		this.tileNumberX = tileNumberX;
		this.tileNumberY = tileNumberY;
		this.shipRegenRate = shipRegenRate;
	}
	
	@Override
	public void consume(JSONObject jsonObject){
		try {
			this.name = jsonObject.getString(Constants.NAME);
			this.shipRegenRate = (float)jsonObject.getDouble(Constants.SHIP_REGEN_RATE);
			this.numberOfShips = jsonObject.getInt(Constants.NUMBER_OF_SHIPS);
			if(jsonObject.has(Constants.OWNER)){
				this.owner = jsonObject.getString(Constants.OWNER);
			}
			JSONObject positionJson = jsonObject.getJSONObject(Constants.POSITION);
			PlanetPosition position = new PlanetPosition();
			position.consume(positionJson);
			this.position = position;
			this.id = jsonObject.getString(Constants.ID);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

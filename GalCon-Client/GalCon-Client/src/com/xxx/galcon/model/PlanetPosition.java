/**
 * 
 */
package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

/**
 * This class is used to hold positional details for a Planet.
 * 
 * @author conormullen
 *
 */
public class PlanetPosition implements JsonConvertible{
	
	private int x;
	private int y;

	public PlanetPosition(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public PlanetPosition(){
		super();
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public void consume(JSONObject jsonObject){
		try {
			this.x = jsonObject.getInt(Constants.X);
			this.y = jsonObject.getInt(Constants.Y);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

/**
 * 
 */
package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConstructable;
import com.xxx.galcon.model.base.JsonConvertible;

/**
 * This class is used to hold positional details for a Planet.
 * 
 * @author conormullen
 *
 */
public class Position extends JsonConvertible implements JsonConstructable{
	
	public int x;
	public int y;

	public Position(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public Position(){
		super();
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

	@Override
	public JSONObject asJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("x", x);
		jsonObject.put("y", y);
		return jsonObject;
	}
}

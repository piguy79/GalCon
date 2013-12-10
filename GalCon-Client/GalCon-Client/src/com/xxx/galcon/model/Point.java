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
public class Point extends JsonConvertible implements JsonConstructable{
	
	public float x;
	public float y;

	public Point(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public Point(){
		super();
	}

	
	
	@Override
	protected void doConsume(JSONObject jsonObject){
		try {
			this.x = Float.parseFloat(jsonObject.getString(Constants.X));
			this.y = Float.parseFloat(jsonObject.getString(Constants.Y));

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

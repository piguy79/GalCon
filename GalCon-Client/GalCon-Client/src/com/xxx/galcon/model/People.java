package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class People extends JsonConvertible {
	
	public List<Player> people = new ArrayList<Player>();

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		JSONArray peopleReturn = jsonObject.optJSONArray(Constants.ITEMS);
		
		if(peopleReturn != null){
			for(int i = 0; i < peopleReturn.length(); i++){
				Player person = new Player(); 
				person.consume( peopleReturn.getJSONObject(i));
				people.add(person);
			}
		}
	}

}

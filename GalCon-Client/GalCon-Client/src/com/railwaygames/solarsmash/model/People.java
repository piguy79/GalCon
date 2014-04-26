package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.MinifiedGame.MinifiedPlayer;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class People extends JsonConvertible {

	public List<MinifiedPlayer> people = new ArrayList<MinifiedPlayer>();

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		JSONArray peopleReturn = jsonObject.optJSONArray(Constants.ITEMS);

		if (peopleReturn != null) {
			for (int i = 0; i < peopleReturn.length(); i++) {
				JSONObject personJson = peopleReturn.getJSONObject(i);
				MinifiedPlayer person = new MinifiedPlayer();
				person.auth = new Auth();
				person.auth.consume(personJson.getJSONObject("auth"));
				person.handle = personJson.optString("handle", "");
				person.xp = personJson.getInt("xp");
				people.add(person);
			}
		}
	}
}

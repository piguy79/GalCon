package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class AvailableGames extends JsonConvertible {
	List<MinifiedGame> allGames = new ArrayList<MinifiedGame>();

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		JSONArray games = jsonObject.optJSONArray(Constants.ITEMS);

		if (games != null) {
			List<JSONException> exceptions = new ArrayList<JSONException>();
			for (int i = 0; i < games.length(); ++i) {
				try {
					MinifiedGame gameBoard = new MinifiedGame();
					gameBoard.consume(games.getJSONObject(i));
					allGames.add(gameBoard);
				} catch (JSONException e) {
					exceptions.add(e);
				}
			}

			if (!exceptions.isEmpty()) {
				throw new JSONException(exceptions.toString());
			}
		}
	}

	public List<MinifiedGame> getAllGames() {
		return allGames;
	}
}

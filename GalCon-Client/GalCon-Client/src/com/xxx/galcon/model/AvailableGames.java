package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class AvailableGames extends JsonConvertible {
	List<GameBoard> allGames = new ArrayList<GameBoard>();

	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		JSONArray games = jsonObject.optJSONArray(Constants.ITEMS);

		if (games != null) {
			List<JSONException> exceptions = new ArrayList<JSONException>();
			for (int i = 0; i < games.length(); ++i) {
				try {
					GameBoard gameBoard = new GameBoard();
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

	public List<GameBoard> getAllGames() {
		return allGames;
	}
}

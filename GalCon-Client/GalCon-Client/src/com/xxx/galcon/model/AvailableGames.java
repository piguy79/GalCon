package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class AvailableGames implements JsonConvertible {
	List<GameBoard> allGames = new ArrayList<GameBoard>();

	@Override
	public void consume(JSONObject jsonObject) {
		try {
			JSONArray games = jsonObject.getJSONArray(Constants.ITEMS);

			for (int i = 0; i < games.length(); ++i) {
				GameBoard gameBoard = new GameBoard();
				gameBoard.consume(games.getJSONObject(i));
				allGames.add(gameBoard);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public List<GameBoard> getAllGames() {
		return allGames;
	}
}

package com.xxx.galcon.http;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.Move;

public class JsonConstructor {

	public static JSONObject performMove(String gameId, List<Move> moves) throws JSONException {
		JSONObject top = new JSONObject();
		top.put("player", GameLoop.USER);
		top.put("id", gameId);
		JSONArray jsonMoves = new JSONArray();

		for (Move move : moves) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("player", GameLoop.USER);
			jsonObject.put("fromPlanet", move.fromPlanet);
			jsonObject.put("toPlanet", move.toPlanet);
			jsonObject.put("fleet", move.shipsToMove);
			jsonObject.put("duration", move.duration);

			jsonMoves.put(jsonObject);
		}
		top.put("moves", jsonMoves);

		return top;
	}

	public static JSONObject generateGame(String player, int width, int height) throws JSONException {
		JSONObject top = new JSONObject();

		top.put("player", player);
		top.put("width", width);
		top.put("height", height);

		return top;
	}
}

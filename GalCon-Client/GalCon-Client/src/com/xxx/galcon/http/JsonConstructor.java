package com.xxx.galcon.http;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.Move;

public class JsonConstructor {

	public static JSONObject performMove(String gameId, List<Move> moves) throws JSONException {
		JSONObject top = new JSONObject();
		top.put("playerHandle", GameLoop.USER.handle);
		top.put("id", gameId);
		JSONArray jsonMoves = new JSONArray();

		for (Move move : moves) {
			jsonMoves.put(move.asJson());
		}
		top.put("moves", jsonMoves);

		return top;
	}

	public static JSONObject generateGame(String playerHandle, int width, int height, String gameType, Long map, Long rankOfInitialPlayer)
			throws JSONException {
		JSONObject top = new JSONObject();

		top.put("playerHandle", playerHandle);
		top.put("width", width);
		top.put("height", height);
		top.put("gameType", gameType);
		top.put("map", map);
		top.put("rankOfInitialPlayer", rankOfInitialPlayer);
		top.put("time", new DateTime(DateTimeZone.UTC).getMillis());

		return top;
	}

	public static JSONObject requestHandle(String userName, String handle) throws JSONException {
		JSONObject top = new JSONObject();

		top.put("userName", userName);
		top.put("handle", handle);

		return top;
	}
	
	public static JSONObject addCoins(String playerHandle, Long numCoins) throws JSONException{
		JSONObject top = new JSONObject();
		
		top.put("playerHandle", playerHandle);
		top.put("numCoins", numCoins);
		
		return top;
		
	}
}

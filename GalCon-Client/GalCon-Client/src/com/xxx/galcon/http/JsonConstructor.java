package com.xxx.galcon.http;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Order;

public class JsonConstructor {

	public static JSONObject performMove(String gameId, List<Move> moves, List<HarvestMove> harvestMoves) throws JSONException {
		JSONObject top = new JSONObject();
		top.put("playerHandle", GameLoop.USER.handle);
		top.put("id", gameId);
		JSONArray jsonMoves = new JSONArray();

		for (Move move : moves) {
			jsonMoves.put(move.asJson());
		}
		top.put("moves", jsonMoves);
		
		if(!harvestMoves.isEmpty()){
			JSONArray jsonHarvestMoves = new JSONArray();
			for(HarvestMove harvestMove : harvestMoves){
				jsonHarvestMoves.put(harvestMove.asJson());
			}
			top.put("harvest", jsonHarvestMoves);
		}
		top.put("time", new DateTime(DateTimeZone.UTC).getMillis());

		return top;
	}

	public static JSONObject matchPlayerToGame(String playerHandle, Long mapToFind)
			throws JSONException {
		JSONObject top = new JSONObject();

		top.put("playerHandle", playerHandle);
		top.put("mapToFind", mapToFind);
		top.put("time", new DateTime(DateTimeZone.UTC).getMillis());

		return top;
	}

	public static JSONObject requestHandle(String userName, String handle) throws JSONException {
		JSONObject top = new JSONObject();

		top.put("userName", userName);
		top.put("handle", handle);

		return top;
	}
	
	public static JSONObject addCoins(String playerHandle, int numCoins) throws JSONException{
		JSONObject top = new JSONObject();
		
		top.put("playerHandle", playerHandle);
		top.put("numCoins", numCoins);
		
		return top;
		
	}
	
	public static JSONObject userWithTime(String playerHandle) throws JSONException{
		JSONObject top = new JSONObject();
		
		top.put("playerHandle", playerHandle);
		top.put("time", new DateTime(DateTimeZone.UTC).getMillis());
		
		return top;
	}
	
	public static JSONObject addCoinsForAnOrder(String playerHandle, List<Order> orders) throws JSONException{
		JSONObject top = new JSONObject();
		
		top.put("playerHandle", playerHandle);
		JSONArray jsonOrders = createOrdersJson(orders);
		top.put("orders", jsonOrders);
		
		return top;
	}
	
	public static JSONObject deleteConsumedOrders(String playerHandle, List<Order> orders) throws JSONException{
		JSONObject top = new JSONObject();
		
		top.put("playerHandle", playerHandle);
		JSONArray jsonOrders = createOrdersJson(orders);
		top.put("orders", jsonOrders);
		
		return top;
	}

	private static JSONArray createOrdersJson(List<Order> orders)
			throws JSONException {
		JSONArray jsonOrders = new JSONArray();
		for(Order order : orders){
			jsonOrders.put(order.asJson());
		}
		return jsonOrders;
	}

	public static JSONObject reduceCall(String playerHandle, Long timeRemaining, Long usedCoins) throws JSONException {
		JSONObject top = new JSONObject();
		
		top.put("playerHandle", playerHandle);
		top.put("timeRemaining", timeRemaining);
		top.put("usedCoins", usedCoins);
		
		return top;
	}
}

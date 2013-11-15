/**
 * 
 */
package com.xxx.galcon.http;

import static com.xxx.galcon.http.UrlConstants.ADD_COINS;
import static com.xxx.galcon.http.UrlConstants.ADD_COINS_FOR_AN_ORDER;
import static com.xxx.galcon.http.UrlConstants.DELETE_CONSUMED_ORDERS;
import static com.xxx.galcon.http.UrlConstants.FIND_ALL_MAPS;
import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_GAMES;
import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_INVENTORY;
import static com.xxx.galcon.http.UrlConstants.FIND_CONFIG_BY_TYPE;
import static com.xxx.galcon.http.UrlConstants.FIND_CURRENT_GAMES_BY_PLAYER_HANDLE;
import static com.xxx.galcon.http.UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE;
import static com.xxx.galcon.http.UrlConstants.FIND_GAME_BY_ID;
import static com.xxx.galcon.http.UrlConstants.FIND_USER_BY_USER_NAME;
import static com.xxx.galcon.http.UrlConstants.JOIN_GAME;
import static com.xxx.galcon.http.UrlConstants.MATCH_PLAYER_TO_GAME;
import static com.xxx.galcon.http.UrlConstants.PERFORM_MOVES;
import static com.xxx.galcon.http.UrlConstants.RECOVER_USED_COINS_COUNT;
import static com.xxx.galcon.http.UrlConstants.REDUCE_TIME;
import static com.xxx.galcon.http.UrlConstants.REQUEST_HANDLE_FOR_USER_NAME;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.request.ClientRequest;
import com.xxx.galcon.http.request.GetClientRequest;
import com.xxx.galcon.http.request.PostClientRequest;
import com.xxx.galcon.inappbilling.util.StoreResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Order;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.base.JsonConvertible;

/**
 * This is a Apache Commons implementation of the GameAction interface.
 * 
 * @author conormullen
 * 
 */
public class DesktopGameAction extends BaseDesktopGameAction implements GameAction {
	

	public DesktopGameAction(String host, int port) {
		super(host, port);
	}

	@Override
	public void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String playerHandle) {

		Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);

		callback.onConnectionResult((AvailableGames) callURL(new GetClientRequest(), FIND_AVAILABLE_GAMES, args,
				new AvailableGames()));
	}

	@Override
	public void findAllMaps(UIConnectionResultCallback<Maps> callback) {
		Map<String, String> args = new HashMap<String, String>();
		callback.onConnectionResult((Maps) callURL(new GetClientRequest(), FIND_ALL_MAPS, args, new Maps()));
	}

	@Override
	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves, List<HarvestMove> harvestMoves) {
		try {
			JSONObject top = JsonConstructor.performMove(gameId, moves, harvestMoves);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((GameBoard) callURL(new PostClientRequest(), PERFORM_MOVES, args,
					new GameBoard()));
		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	public void requestHandleForUserName(UIConnectionResultCallback<HandleResponse> callback, String userName,
			String handle) {
		try {
			JSONObject top = JsonConstructor.requestHandle(userName, handle);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((HandleResponse) callURL(new PostClientRequest(), REQUEST_HANDLE_FOR_USER_NAME,
					args, new HandleResponse()));
		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	@Override
	public void matchPlayerToGame(UIConnectionResultCallback<GameBoard> callback, String playerHandle, Long mapToFind) {
		try {
			JSONObject top = JsonConstructor.matchPlayerToGame(playerHandle, mapToFind);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((GameBoard) callURL(new PostClientRequest(), MATCH_PLAYER_TO_GAME, args,
					new GameBoard()));
		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	@Override
	public void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);
		args.put("id", id);
		callback.onConnectionResult((GameBoard) callURL(new GetClientRequest(), JOIN_GAME, args, new GameBoard()));
	}

	@Override
	public void findGameById(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);
		args.put("playerHandle", playerHandle);
		callback.onConnectionResult((GameBoard) callURL(new GetClientRequest(), FIND_GAME_BY_ID, args, new GameBoard()));
	}

	@Override
	public void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback, String playerHandle) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);
		callback.onConnectionResult((AvailableGames) callURL(new GetClientRequest(),
				FIND_CURRENT_GAMES_BY_PLAYER_HANDLE, args, new AvailableGames()));
	}

	@Override
	public void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String playerHandle)
			throws ConnectionException {
		Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);
		callback.onConnectionResult((AvailableGames) callURL(new GetClientRequest(), FIND_GAMES_WITH_A_PENDING_MOVE,
				args, new AvailableGames()));

	}

	@Override
	public void findUserInformation(UIConnectionResultCallback<Player> callback, String player) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("userName", player);
		callback.onConnectionResult((Player) callURL(new GetClientRequest(), FIND_USER_BY_USER_NAME, args, new Player()));
	}
	
	@Override
	public void findConfigByType(
			final UIConnectionResultCallback<Configuration> callback,final String type) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("type", type);
		callback.onConnectionResult((Configuration) callURL(new GetClientRequest(), FIND_CONFIG_BY_TYPE, args, new Configuration()));
		
	}
	
	@Override
	public void loadAvailableInventory(
			final UIConnectionResultCallback<Inventory> callback) {
		Map<String, String> args = new HashMap<String, String>();
		callback.onConnectionResult((Inventory) callURL(new GetClientRequest(), FIND_AVAILABLE_INVENTORY, args, new Inventory()));
	}
	
	@Override
	public void loadStoreInventory(final Inventory inventory, final StoreResultCallback<Inventory> callback) {
		Inventory stock = new Inventory();
		stock.inventory = inventory.inventory;
		
		InventoryItem inventoryItem =  new InventoryItem("coin_bundle_1", "$1.99", "coin_bundle_1", 2);
		stock.inventory.add(inventoryItem);
		
		inventoryItem =  new InventoryItem("coin_bundle_2", "$2.99", "coin_bundle_2", 6);
		stock.inventory.add(inventoryItem);
		
		callback.onResult(stock);
	}

	private InventoryItem createDummyInventoryItem( ) {
		InventoryItem inventoryItem = new InventoryItem();
		inventoryItem.name = "coin_bundle_1";
		inventoryItem.sku = "coin_bundle_1";
		inventoryItem.price = "$1.99";
		return inventoryItem;
	}

	private JsonConvertible callURL(ClientRequest clientRequest, String path, Map<String, String> parameters,
			JsonConvertible converter) {
		try {
			String postResponse = executeHttpRequest(clientRequest, path, parameters);
			return buildObjectsFromResponse(converter, postResponse);
		} catch (MalformedURLException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} catch (URISyntaxException e) {
			System.out.println(e);
		}

		return null;
	}

	/**
	 * This method us used to populate a GameBoard Object with attributes from a
	 * JsonObject.
	 * 
	 */
	private JsonConvertible buildObjectsFromResponse(JsonConvertible converter, String postResponse) {
		System.out.println(postResponse);

		try {
			JSONObject gameInformation = new JSONObject(postResponse);
			converter.consume(gameInformation);

			return converter;

		} catch (JSONException e) {
			System.out.println(e);
		}

		return null;
	}

	@Override
	public void addCoins(UIConnectionResultCallback<Player> callback, String playerHandle, int numCoins) {
		try {
			JSONObject top = JsonConstructor.addCoins(playerHandle, numCoins);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), ADD_COINS, args, new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}

	}
	
	@Override
	public void addCoinsForAnOrder(UIConnectionResultCallback<Player> callback,
			String playerHandle, List<Order> orders)
			throws ConnectionException {
		try {
			JSONObject top = JsonConstructor.addCoinsForAnOrder(playerHandle, orders);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), ADD_COINS_FOR_AN_ORDER, args, new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}
		
	}
	
	@Override
	public void deleteConsumedOrders(
			UIConnectionResultCallback<Player> callback, String playerHandle,
			List<Order> orders) {
		try {
			JSONObject top = JsonConstructor.deleteConsumedOrders(playerHandle, orders);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), DELETE_CONSUMED_ORDERS, args, new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	@Override
	public void showAd(AdColonyVideoListener listener) {
		// Do nothing right now.
	}
	
	@Override
	public void purchaseCoins(InventoryItem inventoryItem, UIConnectionResultCallback<Player> callback){
		addCoins(callback, GameLoop.USER.handle, inventoryItem.numCoins);
	}

	@Override
	public void reduceTimeUntilNextGame(
			UIConnectionResultCallback<Player> callback, String playerHandle, Long timeRemaining,
			Long usedCoins) throws ConnectionException {
		try {
			JSONObject top = JsonConstructor.reduceCall(playerHandle,timeRemaining, usedCoins);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), REDUCE_TIME, args, new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}
		
	}

	@Override
	public void consumeOrders(List<Order> orders) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void consumeExistingOrders() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recoverUsedCoinCount(
			UIConnectionResultCallback<Player> callback, String playerHandle) {
		try {
			JSONObject top = JsonConstructor.userWithTime(playerHandle);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), RECOVER_USED_COINS_COUNT, args, new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}
		
	}

	

}

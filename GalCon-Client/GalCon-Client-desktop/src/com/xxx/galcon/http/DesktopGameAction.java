/**
 * 
 */
package com.xxx.galcon.http;

import static com.xxx.galcon.http.UrlConstants.ADD_FREE_COINS;
import static com.xxx.galcon.http.UrlConstants.DELETE_CONSUMED_ORDERS;
import static com.xxx.galcon.http.UrlConstants.FIND_ALL_MAPS;
import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_GAMES;
import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_INVENTORY;
import static com.xxx.galcon.http.UrlConstants.FIND_CONFIG_BY_TYPE;
import static com.xxx.galcon.http.UrlConstants.FIND_CURRENT_GAMES_BY_PLAYER_HANDLE;
import static com.xxx.galcon.http.UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE;
import static com.xxx.galcon.http.UrlConstants.FIND_GAME_BY_ID;
import static com.xxx.galcon.http.UrlConstants.FIND_USER_BY_EMAIL;
import static com.xxx.galcon.http.UrlConstants.INVITE_USER_TO_PLAY;
import static com.xxx.galcon.http.UrlConstants.JOIN_GAME;
import static com.xxx.galcon.http.UrlConstants.MATCH_PLAYER_TO_GAME;
import static com.xxx.galcon.http.UrlConstants.PERFORM_MOVES;
import static com.xxx.galcon.http.UrlConstants.RECOVER_USED_COINS_COUNT;
import static com.xxx.galcon.http.UrlConstants.REDUCE_TIME;
import static com.xxx.galcon.http.UrlConstants.REQUEST_HANDLE_FOR_EMAIL;
import static com.xxx.galcon.http.UrlConstants.RESIGN_GAME;
import static com.xxx.galcon.http.UrlConstants.SEARCH_FOR_USERS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.jirbo.adcolony.AdColonyVideoListener;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.request.ClientRequest;
import com.xxx.galcon.http.request.GetClientRequest;
import com.xxx.galcon.http.request.PostClientRequest;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.GameQueue;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Order;
import com.xxx.galcon.model.People;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.Session;
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
	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves,
			List<HarvestMove> harvestMoves) {
		try {
			JSONObject top = JsonConstructor.performMove(gameId, moves, harvestMoves, getSession());

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((GameBoard) callURL(new PostClientRequest(), PERFORM_MOVES, args,
					new GameBoard()));
		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	public void requestHandleForEmail(UIConnectionResultCallback<HandleResponse> callback, String email, String handle) {
		try {
			JSONObject top = JsonConstructor.requestHandle(email, handle, getSession());

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			HandleResponse response = (HandleResponse) callURL(new PostClientRequest(), REQUEST_HANDLE_FOR_EMAIL, args,
					new HandleResponse());

			if (response.valid) {
				callback.onConnectionResult(response);
			} else {
				callback.onConnectionError(response.reason);
			}

		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	@Override
	public void matchPlayerToGame(UIConnectionResultCallback<GameBoard> callback, String playerHandle, Long mapToFind) {
		try {
			JSONObject top = JsonConstructor.matchPlayerToGame(playerHandle, mapToFind, getSession());

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
		args.put("handle", playerHandle);
		args.put("session", getSession());
		callback.onConnectionResult((AvailableGames) callURL(new GetClientRequest(),
				FIND_CURRENT_GAMES_BY_PLAYER_HANDLE, args, new AvailableGames()));
	}

	@Override
	public void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String playerHandle) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);
		callback.onConnectionResult((AvailableGames) callURL(new GetClientRequest(), FIND_GAMES_WITH_A_PENDING_MOVE,
				args, new AvailableGames()));
	}

	@Override
	public void findUserInformation(UIConnectionResultCallback<Player> callback, String email) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("email", email);
		args.put("session", session);
		callback.onConnectionResult((Player) callURL(new GetClientRequest(), FIND_USER_BY_EMAIL, args, new Player()));
	}

	@Override
	public void searchForPlayers(UIConnectionResultCallback<People> callback, String searchTerm) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("searchTerm", searchTerm);
		args.put("session", getSession());
		People people = (People) callURL(new GetClientRequest(), SEARCH_FOR_USERS, args, new People());
		if (people.valid) {
			callback.onConnectionResult(people);
		} else {
			callback.onConnectionError(people.reason);
		}

	}

	@Override
	public void findConfigByType(final UIConnectionResultCallback<Configuration> callback, final String type) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("type", type);
		callback.onConnectionResult((Configuration) callURL(new GetClientRequest(), FIND_CONFIG_BY_TYPE, args,
				new Configuration()));

	}

	@Override
	public void loadAvailableInventory(final UIConnectionResultCallback<Inventory> callback) {
		Map<String, String> args = new HashMap<String, String>();
		callback.onConnectionResult((Inventory) callURL(new GetClientRequest(), FIND_AVAILABLE_INVENTORY, args,
				new Inventory()));
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
	public void addFreeCoins(UIConnectionResultCallback<Player> callback, String playerHandle) {
		try {
			JSONObject top = JsonConstructor.addCoins(playerHandle, getSession());

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), ADD_FREE_COINS, args, new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}

	}

	@Override
	public void addCoinsForAnOrder(UIConnectionResultCallback<Player> callback, String playerHandle, List<Order> orders) {
		try {
			/*
			 * Instead of trying to use the token process on the desktop, insert
			 * a valid session directly into the local DB.
			 */
			MongoClient client = new MongoClient("localhost");
			DB galcon = client.getDB("galcon");
			DBCollection usersCollection = galcon.getCollection("users");

			DBObject user = usersCollection.findOne(new BasicDBObject("email", GameLoop.USER.email));

			for (Order order : orders) {
				GameLoop.USER.coins = GameLoop.USER.coins
						+ DesktopInAppBillingAction.storeItems.get(order.productId).numCoins;
				user.put("coins", GameLoop.USER.coins);
				user.put("usedCoins", -1);
				user.put("watchedAd", false);
				usersCollection.update(new BasicDBObject("email", GameLoop.USER.email), user);
			}
			client.close();

			GameLoop.USER.watchedAd = false;
			GameLoop.USER.usedCoins = -1L;
			callback.onConnectionResult(GameLoop.USER);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteConsumedOrders(UIConnectionResultCallback<Player> callback, String playerHandle,
			List<Order> orders) {
		try {
			JSONObject top = JsonConstructor.deleteConsumedOrders(playerHandle, orders);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), DELETE_CONSUMED_ORDERS, args,
					new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	@Override
	public void showAd(AdColonyVideoListener listener) {
		listener.onAdColonyVideoFinished();
	}

	@Override
	public void reduceTimeUntilNextGame(UIConnectionResultCallback<Player> callback, String playerHandle) {
		try {
			JSONObject top = JsonConstructor.reduceCall(playerHandle, getSession());

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), REDUCE_TIME, args, new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}

	}

	@Override
	public void recoverUsedCoinCount(UIConnectionResultCallback<Player> callback, String playerHandle) {
		try {
			JSONObject top = JsonConstructor.user(playerHandle, getSession());

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), RECOVER_USED_COINS_COUNT, args,
					new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}
	}
	

	@Override
	public void invitePlayerForGame(
			UIConnectionResultCallback<GameBoard> callback,
			String requesterHandle, String inviteeHandle, Long mapKey) {
		try {
			JSONObject top = JsonConstructor.invite(requesterHandle, inviteeHandle, getSession(), mapKey);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((GameBoard) callURL(new PostClientRequest(), INVITE_USER_TO_PLAY, args,
					new GameBoard()));
		} catch (JSONException e) {
			System.out.println(e);
		}
		
	}

	private String session;

	@Override
	public void setSession(String session) {
		this.session = session;
	}

	@Override
	public String getSession() {
		return session;
	}

	@Override
	public void exchangeTokenForSession(UIConnectionResultCallback<Session> callback, String authProvider, String token) {
		try {
			Session session = new Session();
			session.session = "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c00000";
			session.errorMessage = "";

			setSession(session.session);

			/*
			 * Instead of trying to use the token process on the desktop, insert
			 * a valid session directly into the local DB.
			 */
			MongoClient client = new MongoClient("localhost");
			DB galcon = client.getDB("galcon");
			DBCollection usersCollection = galcon.getCollection("users");

			DBObject user = usersCollection.findOne(new BasicDBObject("email", GameLoop.USER.email));
			if (user == null) {

				BasicDBObject newUser = new BasicDBObject("email", GameLoop.USER.email)
						.append("xp", 0)
						.append("wins", 0)
						.append("losses", 0)
						.append("coins", 1)
						.append("usedCoins", -1)
						.append("watchedAd", false)
						.append("auth", new BasicDBObject().append("g", GameLoop.USER.email))
						.append("session",
								new BasicDBObject("id", session.session).append("expireDate",
										new Date(System.currentTimeMillis() + 4 * 60 * 60 * 1000)))
						.append("rankInfo", new BasicDBObject("level", 1).append("startFrom", 0).append("endAt", 50));
				usersCollection.insert(newUser);
			} else {
				user.put(
						"session",
						new BasicDBObject("id", session.session).append("expireDate",
								new Date(System.currentTimeMillis() + 4 * 60 * 60 * 1000)));
				usersCollection.update(new BasicDBObject("email", GameLoop.USER.email), user);
			}
			client.close();

			callback.onConnectionResult(session);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setGameLoop(GameLoop gameLoop) {

	}

	@Override
	public void resignGame(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle) {
		try {
			JSONObject top = JsonConstructor.resignGame(handle, getSession());

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((GameBoard) callURL(new PostClientRequest(),
					RESIGN_GAME.replace(":gameId", gameId), args, new GameBoard()));
		} catch (JSONException e) {
			System.out.println(e);
		}

	}

}

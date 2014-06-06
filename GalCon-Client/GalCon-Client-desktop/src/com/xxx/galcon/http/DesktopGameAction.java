/**
 * 
 */
package com.xxx.galcon.http;

import static com.railwaygames.solarsmash.http.UrlConstants.ACCEPT_INVITE;
import static com.railwaygames.solarsmash.http.UrlConstants.ADD_FREE_COINS;
import static com.railwaygames.solarsmash.http.UrlConstants.ADD_PROVIDER_TO_USER;
import static com.railwaygames.solarsmash.http.UrlConstants.CANCEL_GAME;
import static com.railwaygames.solarsmash.http.UrlConstants.CLAIM_VICTORY;
import static com.railwaygames.solarsmash.http.UrlConstants.DECLINE_INVITE;
import static com.railwaygames.solarsmash.http.UrlConstants.DELETE_CONSUMED_ORDERS;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_ALL_MAPS;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_AVAILABLE_INVENTORY;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_CONFIG_BY_TYPE;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_CURRENT_GAMES_BY_PLAYER_HANDLE;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_FRIENDS;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_GAME_BY_ID;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_LEADERBOARD_BY_ID;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_MATCHING_FRIENDS;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_PENDING_INVITE;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_USER_BY_ID;
import static com.railwaygames.solarsmash.http.UrlConstants.INVITE_USER_TO_PLAY;
import static com.railwaygames.solarsmash.http.UrlConstants.JOIN_GAME;
import static com.railwaygames.solarsmash.http.UrlConstants.MATCH_PLAYER_TO_GAME;
import static com.railwaygames.solarsmash.http.UrlConstants.PERFORM_MOVES;
import static com.railwaygames.solarsmash.http.UrlConstants.PRACTICE;
import static com.railwaygames.solarsmash.http.UrlConstants.REQUEST_HANDLE_FOR_ID;
import static com.railwaygames.solarsmash.http.UrlConstants.RESIGN_GAME;
import static com.railwaygames.solarsmash.http.UrlConstants.SEARCH_FOR_USERS;

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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.config.Configuration;
import com.railwaygames.solarsmash.http.GameAction;
import com.railwaygames.solarsmash.http.JsonConstructor;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.AvailableGames;
import com.railwaygames.solarsmash.model.BaseResult;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.GameCount;
import com.railwaygames.solarsmash.model.GameQueue;
import com.railwaygames.solarsmash.model.HandleResponse;
import com.railwaygames.solarsmash.model.HarvestMove;
import com.railwaygames.solarsmash.model.Inventory;
import com.railwaygames.solarsmash.model.Leaderboards;
import com.railwaygames.solarsmash.model.Maps;
import com.railwaygames.solarsmash.model.Move;
import com.railwaygames.solarsmash.model.Order;
import com.railwaygames.solarsmash.model.People;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.model.Session;
import com.railwaygames.solarsmash.model.base.JsonConvertible;
import com.xxx.galcon.http.request.ClientRequest;
import com.xxx.galcon.http.request.GetClientRequest;
import com.xxx.galcon.http.request.PostClientRequest;

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
	public void findAllMaps(UIConnectionResultCallback<Maps> callback) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("version", Constants.MAP_VERSION_SUPPORTED);
		callback.onConnectionResult((Maps) callURL(new GetClientRequest(), FIND_ALL_MAPS, args, new Maps()));
	}

	@Override
	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves,
			List<HarvestMove> harvestMoves) {
		try {
			JSONObject top = JsonConstructor.performMove(gameId, moves, harvestMoves, getSession());

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			GameBoard result = (GameBoard) callURL(new PostClientRequest(), PERFORM_MOVES, args, new GameBoard());

			if (result == null) {
				callback.onConnectionError("Invalid Move");
			} else {
				callback.onConnectionResult(result);
			}

		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	public void requestHandleForId(UIConnectionResultCallback<HandleResponse> callback, String id, String handle,
			String authProvider) {
		try {
			JSONObject top = JsonConstructor.requestHandle(id, handle, getSession(), authProvider);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			HandleResponse response = (HandleResponse) callURL(new PostClientRequest(), REQUEST_HANDLE_FOR_ID, args,
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

			GameBoard result = (GameBoard) callURL(new PostClientRequest(), MATCH_PLAYER_TO_GAME, args, new GameBoard());

			if (result == null) {
				callback.onConnectionError("Unable to join game.");
			} else {
				callback.onConnectionResult(result);
			}
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
		args.put("handle", playerHandle);
		args.put("session", getSession());
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
	public void findGamesWithPendingMove(UIConnectionResultCallback<GameCount> callback, String playerHandle) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("handle", playerHandle);
		callback.onConnectionResult((GameCount) callURL(new GetClientRequest(), FIND_GAMES_WITH_A_PENDING_MOVE, args,
				new GameCount()));
	}

	@Override
	public void findUserInformation(UIConnectionResultCallback<Player> callback, String id, String authProvider) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);
		args.put("authProvider", authProvider);
		args.put("session", session);
		callback.onConnectionResult((Player) callURL(new GetClientRequest(), FIND_USER_BY_ID, args, new Player()));
	}

	@Override
	public void searchForPlayers(UIConnectionResultCallback<People> callback, String searchTerm) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("searchTerm", searchTerm);
		args.put("session", getSession());
		args.put("handle", GameLoop.USER.handle);
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
			System.out.println(e.getStackTrace());
		} catch (IOException e) {
			System.out.println(e.getStackTrace());
		} catch (URISyntaxException e) {
			System.out.println(e.getStackTrace());
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
			System.out.println(e.getStackTrace());
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

			Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
			String authProvider = prefs.getString(Constants.Auth.SOCIAL_AUTH_PROVIDER);

			DBObject user = usersCollection.findOne(new BasicDBObject("auth." + authProvider, GameLoop.USER.auth
					.getID(authProvider)));

			for (Order order : orders) {
				GameLoop.USER.coins = GameLoop.USER.coins
						+ DesktopInAppBillingAction.storeItems.get(order.productId).numCoins;
				user.put("coins", GameLoop.USER.coins);
				user.put("usedCoins", -1);
				user.put("watchedAd", false);
				user.put("na", true);
				usersCollection.update(
						new BasicDBObject("auth." + authProvider, GameLoop.USER.auth.getID(authProvider)), user);
			}
			client.close();
			callback.onConnectionResult(GameLoop.USER);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteConsumedOrders(UIConnectionResultCallback<Player> callback, String playerHandle,
			List<Order> orders) {
		try {
			JSONObject top = JsonConstructor.deleteConsumedOrders(playerHandle, orders, getSession());

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), DELETE_CONSUMED_ORDERS, args,
					new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	@Override
	public void showAd() {
	}

	@Override
	public void invitePlayerForGame(UIConnectionResultCallback<GameBoard> callback, String requesterHandle,
			String inviteeHandle, Long mapKey) {
		try {
			JSONObject top = JsonConstructor.invite(requesterHandle, inviteeHandle, getSession(), mapKey);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			GameBoard result = (GameBoard) callURL(new PostClientRequest(), INVITE_USER_TO_PLAY, args, new GameBoard());
			if (result == null) {
				callback.onConnectionError("Unable to create game.");
			} else {
				callback.onConnectionResult(result);
			}
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
			DB galcon = client.getDB("app14217106");
			DBCollection usersCollection = galcon.getCollection("users");

			DBObject user = usersCollection.findOne(new BasicDBObject("auth." + authProvider, GameLoop.USER.auth
					.getID(authProvider)));
			if (user == null) {

				BasicDBObject newUser = new BasicDBObject("auth", new BasicDBObject(authProvider,
						GameLoop.USER.auth.getID(authProvider)))
						.append("xp", 6999)
						.append("wins", 0)
						.append("losses", 0)
						.append("coins", 1)
						.append("usedCoins", -1)
						.append("watchedAd", false)
						.append("session",
								new BasicDBObject("id", session.session).append("expireDate",
										new Date(System.currentTimeMillis() + 4 * 60 * 60 * 1000)))
						.append("rankInfo", new BasicDBObject("level", 15).append("startFrom", 0).append("endAt", 50));
				usersCollection.insert(newUser);
			} else {
				user.put(
						"session",
						new BasicDBObject("id", session.session).append("expireDate",
								new Date(System.currentTimeMillis() + 4 * 60 * 60 * 1000)));
				usersCollection.update(
						new BasicDBObject("auth." + authProvider, GameLoop.USER.auth.getID(authProvider)), user);
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

	@Override
	public void findFriends(UIConnectionResultCallback<People> callback, String handle) {

		Map<String, String> args = new HashMap<String, String>();
		args.put("session", getSession());
		args.put("handle", handle);
		People people = (People) callURL(new GetClientRequest(), FIND_FRIENDS, args, new People());
		if (people.valid) {
			callback.onConnectionResult(people);
		} else {
			callback.onConnectionError(people.reason);
		}
	}

	@Override
	public void findPendingIvites(UIConnectionResultCallback<GameQueue> callback, String handle) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("session", getSession());
		args.put("handle", handle);
		GameQueue queue = (GameQueue) callURL(new GetClientRequest(), FIND_PENDING_INVITE, args, new GameQueue());
		if (queue.valid) {
			callback.onConnectionResult(queue);
		} else {
			callback.onConnectionError(queue.reason);
		}

	}

	@Override
	public void acceptInvite(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		args.put("gameId", gameId);
		args.put("session", getSession());
		GameBoard gameBoard = (GameBoard) callURL(new GetClientRequest(), ACCEPT_INVITE, args, new GameBoard());
		if (gameBoard != null) {
			callback.onConnectionResult(gameBoard);
		} else {
			callback.onConnectionError("Game does not exist");
		}

	}

	@Override
	public void declineInvite(UIConnectionResultCallback<BaseResult> callback, String gameId, String handle) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		args.put("gameId", gameId);
		args.put("session", getSession());
		callback.onConnectionResult((BaseResult) callURL(new GetClientRequest(), DECLINE_INVITE, args, new BaseResult()));

	}

	@Override
	public void findMatchingFriends(UIConnectionResultCallback<People> callback, List<String> authIds, String handle,
			String authProvider) {
		try {
			JSONObject top = JsonConstructor.matchingFriends(authIds, handle, getSession(), authProvider);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((People) callURL(new PostClientRequest(), FIND_MATCHING_FRIENDS, args,
					new People()));
		} catch (JSONException e) {
			System.out.println(e);
		}

	}

	@Override
	public void addProviderToUser(UIConnectionResultCallback<Player> callback, String handle, String id,
			String authProvider) {
		try {
			JSONObject top = JsonConstructor.addProvider(handle, id, getSession(), authProvider);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), ADD_PROVIDER_TO_USER, args,
					new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}

	}

	@Override
	public void cancelGame(UIConnectionResultCallback<BaseResult> callback, String handle, String gameId) {
		try {
			JSONObject top = JsonConstructor.cancelGame(handle, gameId, getSession());

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((BaseResult) callURL(new PostClientRequest(), CANCEL_GAME, args,
					new BaseResult()));
		} catch (JSONException e) {
			System.out.println(e);
		}

	}

	@Override
	public void claimVictory(UIConnectionResultCallback<GameBoard> callback, String handle, String gameId) {
		try {
			JSONObject top = JsonConstructor.claimGame(handle, gameId, getSession());

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			GameBoard gameBoard = (GameBoard) callURL(new PostClientRequest(), CLAIM_VICTORY, args, new GameBoard());

			if (gameBoard == null) {
				callback.onConnectionError("Invalid claim");
			} else {
				callback.onConnectionResult(gameBoard);
			}

		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	@Override
	public void findLeaderboardById(final UIConnectionResultCallback<Leaderboards> callback, final String id) {
		final Map<String, String> args = new HashMap<String, String>();
		callback.onConnectionResult((Leaderboards) callURL(new GetClientRequest(),
				FIND_LEADERBOARD_BY_ID.replace(":id", id), args, new Leaderboards()));
	}

	@Override
	public void practiceGame(UIConnectionResultCallback<GameBoard> callback, String handle, Long mapId) {
		try {
			JSONObject top = JsonConstructor.practiceGame(handle, getSession(), mapId);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			GameBoard gameBoard = (GameBoard) callURL(new PostClientRequest(), PRACTICE, args, new GameBoard());

			if (gameBoard == null) {
				callback.onConnectionError("Invalid claim");
			} else {
				callback.onConnectionResult(gameBoard);
			}

		} catch (JSONException e) {
			System.out.println(e);
		}

	}

}

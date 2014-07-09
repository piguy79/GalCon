package com.railwaygames.solarsmash;

import static com.railwaygames.solarsmash.Config.HOST;
import static com.railwaygames.solarsmash.Config.PORT;
import static com.railwaygames.solarsmash.Config.PROTOCOL;
import static com.railwaygames.solarsmash.Constants.CONNECTION_ERROR_MESSAGE;
import static com.railwaygames.solarsmash.Constants.GALCON_PREFS;
import static com.railwaygames.solarsmash.http.UrlConstants.ACCEPT_INVITE;
import static com.railwaygames.solarsmash.http.UrlConstants.ADD_COINS_FOR_AN_ORDER;
import static com.railwaygames.solarsmash.http.UrlConstants.ADD_FREE_COINS;
import static com.railwaygames.solarsmash.http.UrlConstants.ADD_PROVIDER_TO_USER;
import static com.railwaygames.solarsmash.http.UrlConstants.CANCEL_GAME;
import static com.railwaygames.solarsmash.http.UrlConstants.CLAIM_VICTORY;
import static com.railwaygames.solarsmash.http.UrlConstants.DELETE_CONSUMED_ORDERS;
import static com.railwaygames.solarsmash.http.UrlConstants.EXCHANGE_TOKEN_FOR_SESSION;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_ALL_MAPS;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_AVAILABLE_INVENTORY;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_CONFIG_BY_TYPE;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_CURRENT_GAMES_BY_PLAYER_HANDLE;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_FRIENDS;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_GAME_BY_ID;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_LEADERBOARDS_FOR_FRIENDS;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSURLResponse;
import org.robovm.bindings.adcolony.AdColony;
import org.robovm.bindings.adcolony.AdColonyAdDelegateAdapter;
import org.robovm.objc.block.VoidBlock3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.railwaygames.solarsmash.config.Configuration;
import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.Connection;
import com.railwaygames.solarsmash.http.GameAction;
import com.railwaygames.solarsmash.http.GameActionCache;
import com.railwaygames.solarsmash.http.JsonConstructor;
import com.railwaygames.solarsmash.http.SocialAction;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.http.UrlConstants;
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

public class IOSGameAction implements GameAction {

	private static final long ONE_DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
	private static final long ONE_HOUR_IN_MILLIS = 1000 * 60 * 60;

	private GameLoop gameLoop;
	private Config config = new IOSConfig();
	private String session = "";
	private SocialAction socialAction;

	public IOSGameAction(SocialAction socialAction) {
		this.socialAction = socialAction;
	}

	@Override
	public void setGameLoop(GameLoop gameLoop) {
		this.gameLoop = gameLoop;
	}

	@Override
	public String getSession() {
		return session;
	}

	@Override
	public void setSession(final String session) {
		this.session = session;

		Gdx.app.postRunnable(new Runnable() {
			public void run() {
				Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
				prefs.putString(Constants.Auth.LAST_SESSION_ID, session);
				prefs.flush();
			}
		});
	}

	@Override
	public void exchangeTokenForSession(final UIConnectionResultCallback<Session> callback, String authProvider,
			String token) {
		try {
			final JSONObject top = JsonConstructor.exchangeToken(authProvider, token);
			new PostJsonRequestTask<Session>(callback, EXCHANGE_TOKEN_FOR_SESSION, Session.class).execute(top
					.toString());
		} catch (JSONException e) {
			Foundation.log(e.getMessage());
		}
	}

	@Override
	public void matchPlayerToGame(UIConnectionResultCallback<GameBoard> callback, String handle, Long mapToFind) {
		try {
			final JSONObject top = JsonConstructor.matchPlayerToGame(handle, mapToFind, getSession());
			new PostJsonRequestTask<GameBoard>(callback, MATCH_PLAYER_TO_GAME, GameBoard.class).execute(top.toString());
		} catch (JSONException e) {
			Foundation.log(e.getMessage());
		}
	}

	private GameActionCache<Maps> mapCache = new GameActionCache<Maps>(ONE_DAY_IN_MILLIS);

	@Override
	public void findAllMaps(UIConnectionResultCallback<Maps> callback) {
		if (mapCache.getCache() != null) {
			callback.onConnectionResult(mapCache.getCache());
		} else {
			final Map<String, String> args = new HashMap<String, String>();
			args.put("version", Constants.MAP_VERSION_SUPPORTED);
			mapCache.setDelegate(callback);
			new GetJsonRequestTask<Maps>(args, mapCache, FIND_ALL_MAPS, Maps.class).execute("");
		}
	}

	@Override
	public void findGameById(UIConnectionResultCallback<GameBoard> callback, String id, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);
		args.put("handle", handle);
		args.put("session", getSession());
		new GetJsonRequestTask<GameBoard>(args, callback, FIND_GAME_BY_ID, GameBoard.class).execute("");
	}

	@Override
	public void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		args.put("id", id);

		new GetJsonRequestTask<GameBoard>(args, callback, JOIN_GAME, GameBoard.class).execute("");
	}

	@Override
	public void resignGame(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle) {
		try {
			final JSONObject top = JsonConstructor.resignGame(handle, getSession());

			new PostJsonRequestTask<GameBoard>(callback, RESIGN_GAME.replace(":gameId", gameId), GameBoard.class)
					.execute(top.toString());

		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves,
			List<HarvestMove> harvestMoves) {
		try {
			final JSONObject top = JsonConstructor.performMove(gameId, moves, harvestMoves, getSession());
			new PostJsonRequestTask<GameBoard>(callback, PERFORM_MOVES, GameBoard.class).execute(top.toString());
		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		args.put("session", getSession());
		new GetJsonRequestTask<AvailableGames>(args, callback, FIND_CURRENT_GAMES_BY_PLAYER_HANDLE,
				AvailableGames.class).execute("");
	}

	@Override
	public void searchForPlayers(UIConnectionResultCallback<People> callback, String searchTerm) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("searchTerm", searchTerm);
		args.put("session", getSession());
		args.put("handle", GameLoop.USER.handle);
		new GetJsonRequestTask<People>(args, callback, SEARCH_FOR_USERS, People.class).execute("");
	}

	@Override
	public void findConfigByType(UIConnectionResultCallback<Configuration> callback, String type) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("type", type);
		new GetJsonRequestTask<Configuration>(args, callback, FIND_CONFIG_BY_TYPE, Configuration.class).execute("");
	}

	@Override
	public void findGamesWithPendingMove(UIConnectionResultCallback<GameCount> callback, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		new GetJsonRequestTask<GameCount>(args, callback, FIND_GAMES_WITH_A_PENDING_MOVE, GameCount.class).execute("");
	}

	@Override
	public void addFreeCoins(UIConnectionResultCallback<Player> callback, String handle) {
		try {
			final JSONObject top = JsonConstructor.addCoins(handle, getSession());
			new PostJsonRequestTask<Player>(callback, ADD_FREE_COINS, Player.class).execute(top.toString());
		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void addCoinsForAnOrder(UIConnectionResultCallback<Player> callback, String handle, List<Order> orders) {
		try {
			final JSONObject top = JsonConstructor.addCoinsForAnOrder(handle, orders, getSession());
			new PostJsonRequestTask<Player>(callback, ADD_COINS_FOR_AN_ORDER, Player.class).execute(top.toString());
		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void deleteConsumedOrders(UIConnectionResultCallback<Player> callback, String handle, List<Order> orders) {
		try {
			final JSONObject top = JsonConstructor.deleteConsumedOrders(handle, orders, getSession());
			new PostJsonRequestTask<Player>(callback, DELETE_CONSUMED_ORDERS, Player.class).execute(top.toString());
		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	private GameActionCache<Inventory> inventoryCache = new GameActionCache<Inventory>(ONE_DAY_IN_MILLIS);

	@Override
	public void loadAvailableInventory(UIConnectionResultCallback<Inventory> callback) {
		if (inventoryCache.getCache() != null) {
			callback.onConnectionResult(inventoryCache.getCache());
		} else {
			inventoryCache.setDelegate(callback);
			new GetJsonRequestTask<Inventory>(new HashMap<String, String>(), inventoryCache, FIND_AVAILABLE_INVENTORY,
					Inventory.class).execute("");
		}
	}

	@Override
	public void invitePlayerForGame(UIConnectionResultCallback<GameBoard> callback, String requesterHandle,
			String inviteeHandle, Long mapKey) {
		try {
			final JSONObject top = JsonConstructor.invite(requesterHandle, inviteeHandle, getSession(), mapKey);
			new PostJsonRequestTask<GameBoard>(callback, INVITE_USER_TO_PLAY, GameBoard.class).execute(top.toString());
		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void findFriends(UIConnectionResultCallback<People> callback, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("session", getSession());
		args.put("handle", handle);
		new GetJsonRequestTask<People>(args, callback, FIND_FRIENDS, People.class).execute("");
	}

	@Override
	public void findPendingIvites(UIConnectionResultCallback<GameQueue> callback, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("session", getSession());
		args.put("handle", handle);
		new GetJsonRequestTask<GameQueue>(args, callback, FIND_PENDING_INVITE, GameQueue.class).execute("");
	}

	@Override
	public void acceptInvite(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		args.put("gameId", gameId);
		args.put("session", getSession());
		new GetJsonRequestTask<GameBoard>(args, callback, ACCEPT_INVITE, GameBoard.class).execute("");
	}

	@Override
	public void declineInvite(UIConnectionResultCallback<BaseResult> callback, String gameId, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		args.put("gameId", gameId);
		args.put("session", getSession());
		new GetJsonRequestTask<BaseResult>(args, callback, ACCEPT_INVITE, BaseResult.class).execute("");
	}

	private class PostJsonRequestTask<T extends JsonConvertible> extends JsonRequestTask<T> {

		public PostJsonRequestTask(UIConnectionResultCallback<T> callback, String path, Class<T> converterClass) {
			super(callback, path, converterClass, null);
		}

		@Override
		public void establishConnection(VoidBlock3<NSURLResponse, NSData, NSError> completionHandler, String... params)
				throws IOException {
			Connection.establishPostConnection(completionHandler, config.getValue(PROTOCOL), config.getValue(HOST),
					config.getValue(PORT), path, params);
		}
	}

	private class GetJsonRequestTask<T extends JsonConvertible> extends JsonRequestTask<T> {
		private Map<String, String> args;

		public GetJsonRequestTask(Map<String, String> args, UIConnectionResultCallback<T> callback, String path,
				Class<T> converterClass) {
			super(callback, path, converterClass, args);
			this.args = args;
		}

		@Override
		public void establishConnection(VoidBlock3<NSURLResponse, NSData, NSError> completionHandler, String... params)
				throws IOException {
			Connection.establishGetConnection(completionHandler, config.getValue(PROTOCOL), config.getValue(HOST),
					config.getValue(PORT), path, args);
		}
	}

	public static void processResponse(JsonConvertible converter, NSData data, NSError error) {
		if (error != null) {
			converter.errorMessage = "Error connecting";
		} else {
			try {
				String json = new String(data.getBytes(), "UTF-8");
				// String sData = data.toBase64EncodedString(new
				// NSDataBase64EncodingOptions(0L));
				// String json = new String(Base64.decode(sData));

				JSONObject returnObject = new JSONObject(json);
				String errorOccurred = returnObject.optString("error");
				if (errorOccurred != null && errorOccurred.trim().length() > 0) {
					converter.errorMessage = errorOccurred;
				} else {
					converter.consume(new JSONObject(json));
				}
			} catch (Exception e) {
				Foundation.log(e.getMessage());
				converter.errorMessage = CONNECTION_ERROR_MESSAGE;
			}
		}
	}

	private abstract class JsonRequestTask<T extends JsonConvertible> {

		protected String path;
		private JsonConvertible converter;
		private UIConnectionResultCallback<T> callback;

		private RequestParams<T> savedParams = new RequestParams<T>();

		public JsonRequestTask(UIConnectionResultCallback<T> callback, String path, Class<T> converterClass,
				Map<String, String> args) {
			this.path = path;
			try {
				this.converter = converterClass.newInstance();
			} catch (InstantiationException e) {
				Foundation.log("Could not create converter class");
			} catch (IllegalAccessException e) {
				Foundation.log("Could not create converter class");
			}
			this.callback = callback;

			savedParams.path = path;
			savedParams.converter = converterClass;
			savedParams.callback = callback;
			savedParams.args = args;
		}

		protected abstract void establishConnection(VoidBlock3<NSURLResponse, NSData, NSError> completionHandler,
				String... params) throws IOException;

		public void execute(final String... params) {
			try {
				savedParams.params = params;
				Foundation.log("Invoking call at path: " + path + ", " + Arrays.toString(params));

				VoidBlock3<NSURLResponse, NSData, NSError> completionHandlerBlock = new VoidBlock3<NSURLResponse, NSData, NSError>() {
					@Override
					public void invoke(NSURLResponse response, NSData data, NSError error) {
						processResponse(converter, data, error);
						onPostExecute(converter);
					}
				};

				establishConnection(completionHandlerBlock, params);
			} catch (IOException e) {
				Foundation.log("Error connecting: " + e.getMessage());
				converter.errorMessage = CONNECTION_ERROR_MESSAGE;
			}
		}

		protected void onPostExecute(final JsonConvertible result) {
			if (result.errorMessage != null && !result.errorMessage.isEmpty()) {
				Foundation.log("Call failed with error: " + result.errorMessage);
				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						callback.onConnectionError(result.errorMessage);
					}
				});
			} else if (!result.valid) {
				callback.onConnectionError(result.reason);
			} else {
				if (result.sessionExpired) {
					Foundation.log("Session expired, beginning silent sign in");
					Gdx.app.postRunnable(new Runnable() {
						public void run() {
							Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
							prefs.putString(Constants.Auth.LAST_SESSION_ID, "");
							prefs.flush();

							socialAction.getToken(new SilentSignInAuthenticationListener<T>(savedParams));
						}
					});
				} else {
					Foundation.log("Call succeeded");
					Gdx.app.postRunnable(new Runnable() {
						public void run() {
							callback.onConnectionResult((T) result);
						}
					});
				}
			}
		}
	}

	private class RequestParams<T> {
		public Map<String, String> args;
		public String path;
		public Class<T> converter;
		public UIConnectionResultCallback<T> callback;
		public String[] params;
	}

	private class SilentSignInAuthenticationListener<T extends JsonConvertible> implements AuthenticationListener {

		private RequestParams<T> savedRequestParams;

		public SilentSignInAuthenticationListener(RequestParams<T> savedRequestParams) {
			this.savedRequestParams = savedRequestParams;
		}

		@Override
		public void onSignOut() {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSignInSucceeded(String authProvider, String token) {
			Foundation.log("Silent sign in succeeded.  Getting session...");
			IOSGameAction.this.exchangeTokenForSession(new UIConnectionResultCallback<Session>() {

				@Override
				public void onConnectionResult(Session result) {
					Foundation.log("Silent sign in succeeded.  Session retrieved.");
					IOSGameAction.this.setSession(result.session);
					if (savedRequestParams.args != null) {
						savedRequestParams.args.put("session", getSession());
						new GetJsonRequestTask<T>(savedRequestParams.args, savedRequestParams.callback,
								savedRequestParams.path, savedRequestParams.converter).execute("");
					} else {
						try {
							JSONObject object = new JSONObject(savedRequestParams.params[0]);
							object.put("session", getSession());

							new PostJsonRequestTask<T>(savedRequestParams.callback, savedRequestParams.path,
									savedRequestParams.converter).execute(object.toString());
						} catch (JSONException e) {
							Foundation.log("Could not reconstructor json request");
							gameLoop.reset();
						}
					}
				}

				@Override
				public void onConnectionError(String msg) {
					Foundation.log("Silent sign in failed on retreiving token with error: " + msg);
					savedRequestParams.callback.onConnectionError(Strings.CONNECTION_FAIL);
				}
			}, authProvider, token);
		}

		@Override
		public void onSignInFailed(String failureMessage) {
			Foundation.log("Silent sign in failed with error: " + failureMessage);
			gameLoop.reset();
		}
	}

	@Override
	public void findUserInformation(UIConnectionResultCallback<Player> callback, String id, String authProvider) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);
		args.put("authProvider", authProvider);
		args.put("session", getSession());
		new GetJsonRequestTask<Player>(args, callback, FIND_USER_BY_ID, Player.class).execute("");
	}

	@Override
	public void requestHandleForId(UIConnectionResultCallback<HandleResponse> callback, String id, String handle,
			String authProvider) {
		try {
			final JSONObject top = JsonConstructor.requestHandle(id, handle, getSession(), authProvider);
			new PostJsonRequestTask<HandleResponse>(callback, REQUEST_HANDLE_FOR_ID, HandleResponse.class).execute(top
					.toString());
		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void findMatchingFriends(UIConnectionResultCallback<People> callback, List<String> authIds, String handle,
			String authProvider) {
		try {
			final JSONObject top = JsonConstructor.matchingFriends(authIds, handle, getSession(), authProvider);
			new PostJsonRequestTask<People>(callback, FIND_MATCHING_FRIENDS, People.class).execute(top.toString());
		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void addProviderToUser(UIConnectionResultCallback<Player> callback, String handle, String id,
			String authProvider) {
		try {
			final JSONObject top = JsonConstructor.addProvider(handle, id, getSession(), authProvider);
			new PostJsonRequestTask<Player>(callback, ADD_PROVIDER_TO_USER, Player.class).execute(top.toString());
		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void cancelGame(UIConnectionResultCallback<BaseResult> callback, String handle, String gameId) {
		try {
			final JSONObject top = JsonConstructor.cancelGame(handle, gameId, getSession());
			new PostJsonRequestTask<BaseResult>(callback, CANCEL_GAME, BaseResult.class).execute(top.toString());
		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void claimVictory(UIConnectionResultCallback<GameBoard> callback, String handle, String gameId) {
		try {
			final JSONObject top = JsonConstructor.claimGame(handle, gameId, session);
			new PostJsonRequestTask<GameBoard>(callback, CLAIM_VICTORY, GameBoard.class).execute(top.toString());
		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void showAd() {
		AdColony.playVideoAd(SolarSmashIOS.ZONE_ID, new AdColonyAdDelegateAdapter());
	}

	@Override
	public void findLeaderboardById(UIConnectionResultCallback<Leaderboards> callback, String id) {
		final Map<String, String> args = new HashMap<String, String>();
		new GetJsonRequestTask<Leaderboards>(args, callback, UrlConstants.FIND_LEADERBOARD_BY_ID.replace(":id", id),
				Leaderboards.class).execute("");
	}

	private Map<String, GameActionCache<Leaderboards>> friendLeaderboardCaches = new ConcurrentHashMap<String, GameActionCache<Leaderboards>>();

	@Override
	public void findLeaderboardsForFriends(UIConnectionResultCallback<Leaderboards> callback, List<String> authIds,
			String handle, String authProvider) {
		GameActionCache<Leaderboards> cache = friendLeaderboardCaches.get(authProvider);
		if (cache == null) {
			cache = new GameActionCache<Leaderboards>(ONE_HOUR_IN_MILLIS);
			friendLeaderboardCaches.put(authProvider, cache);
		}

		if (cache.getCache() != null) {
			callback.onConnectionResult(cache.getCache());
		} else {
			cache.setDelegate(callback);
			try {
				final JSONObject top = JsonConstructor.leaderBoardsForFriends(authIds, handle, getSession(),
						authProvider);
				final GameActionCache<Leaderboards> fCache = cache;

				new PostJsonRequestTask<Leaderboards>(fCache, FIND_LEADERBOARDS_FOR_FRIENDS, Leaderboards.class)
						.execute(top.toString());
			} catch (JSONException e) {
				Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
			}
		}
	}

	@Override
	public void practiceGame(UIConnectionResultCallback<GameBoard> callback, String handle, Long mapId) {
		try {
			final JSONObject top = JsonConstructor.practiceGame(handle, session, mapId);
			new PostJsonRequestTask<GameBoard>(callback, PRACTICE, GameBoard.class).execute(top.toString());
		} catch (JSONException e) {
			Foundation.log("This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}
}

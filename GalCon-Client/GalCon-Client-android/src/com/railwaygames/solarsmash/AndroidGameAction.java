package com.railwaygames.solarsmash;

import static com.railwaygames.solarsmash.Config.HOST;
import static com.railwaygames.solarsmash.Config.PORT;
import static com.railwaygames.solarsmash.Config.PROTOCOL;
import static com.railwaygames.solarsmash.Constants.CONNECTION_ERROR_MESSAGE;
import static com.railwaygames.solarsmash.Constants.GALCON_PREFS;
import static com.railwaygames.solarsmash.MainActivity.LOG_NAME;
import static com.railwaygames.solarsmash.http.UrlConstants.ACCEPT_INVITE;
import static com.railwaygames.solarsmash.http.UrlConstants.ADD_COINS_FOR_AN_ORDER;
import static com.railwaygames.solarsmash.http.UrlConstants.ADD_FREE_COINS;
import static com.railwaygames.solarsmash.http.UrlConstants.ADD_PROVIDER_TO_USER;
import static com.railwaygames.solarsmash.http.UrlConstants.CANCEL_GAME;
import static com.railwaygames.solarsmash.http.UrlConstants.CLAIM_VICTORY;
import static com.railwaygames.solarsmash.http.UrlConstants.DECLINE_INVITE;
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
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.crashlytics.android.Crashlytics;
import com.railwaygames.solarsmash.config.Configuration;
import com.railwaygames.solarsmash.http.AuthenticationListener;
import com.railwaygames.solarsmash.http.GameAction;
import com.railwaygames.solarsmash.http.GameActionCache;
import com.railwaygames.solarsmash.http.JsonConstructor;
import com.railwaygames.solarsmash.http.SocialAction;
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
import com.railwaygames.solarsmash.model.PlayerList;
import com.railwaygames.solarsmash.model.Session;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class AndroidGameAction implements GameAction {
	private static final String TAG = "GameAction";
	private static final String OS = "android";
	private ConnectivityManager connectivityManager;
	private MainActivity activity;
	private SocialAction socialAction;
	private GameLoop gameLoop;
	private Config config = new AndroidConfig();

	private static final long ONE_DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
	private static final long ONE_HOUR_IN_MILLIS = 1000 * 60 * 60;

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
			Log.i(TAG, "Silent sign in succeeded.  Getting session...");
			AndroidGameAction.this.exchangeTokenForSession(new UIConnectionResultCallback<Session>() {

				@Override
				public void onConnectionResult(Session result) {
					Log.i(TAG, "Silent sign in succeeded.  Session retrieved.");
					AndroidGameAction.this.setSession(result.session);
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
							Log.e(TAG, "Could not reconstructor json request", e);
							Crashlytics.logException(e);
							gameLoop.reset();
						}
					}
				}

				@Override
				public void onConnectionError(String msg) {
					Log.w(TAG, "Silent sign in failed on retreiving token with error: " + msg);
					savedRequestParams.callback.onConnectionError(Strings.CONNECTION_FAIL);
				}
			}, authProvider, token);
		}

		@Override
		public void onSignInFailed(String failureMessage) {
			Log.w(TAG, "Silent sign in failed with error: " + failureMessage);
			gameLoop.reset();
		}

	}

	private String session = "";

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

	public void setGameLoop(GameLoop gameLoop) {
		this.gameLoop = gameLoop;
	}

	public AndroidGameAction(MainActivity activity, SocialAction socialAction, ConnectivityManager connectivityManager) {
		this.connectivityManager = connectivityManager;
		this.activity = activity;
		this.socialAction = socialAction;
	}

	private GameActionCache<Maps> mapCache = new GameActionCache<Maps>(ONE_DAY_IN_MILLIS);

	@Override
	public void findAllMaps(final UIConnectionResultCallback<Maps> callback) {
		if (mapCache.getCache() != null) {
			callback.onConnectionResult(mapCache.getCache());
		} else {
			mapCache.setDelegate(callback);
			final Map<String, String> args = new HashMap<String, String>();
			args.put("version", Constants.MAP_VERSION_SUPPORTED);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new GetJsonRequestTask<Maps>(args, mapCache, FIND_ALL_MAPS, Maps.class).execute("");
				}
			});
		}
	}

	public void joinGame(final UIConnectionResultCallback<GameBoard> callback, String id, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		args.put("id", id);

		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<GameBoard>(args, callback, JOIN_GAME, GameBoard.class).execute("");
			}
		});
	}

	@Override
	public void matchPlayerToGame(final UIConnectionResultCallback<GameBoard> callback, String handle, Long mapToFind) {
		try {
			final JSONObject top = JsonConstructor.matchPlayerToGame(handle, mapToFind, getSession());

			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<GameBoard>(callback, MATCH_PLAYER_TO_GAME, GameBoard.class).execute(top
							.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}

	}

	public void performMoves(final UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves,
			List<HarvestMove> harvestMoves) {
		try {
			final JSONObject top = JsonConstructor.performMove(gameId, moves, harvestMoves, getSession());
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<GameBoard>(callback, PERFORM_MOVES, GameBoard.class).execute(top.toString());
					NotificationManager mNotificationManager = (NotificationManager) activity
							.getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancel(PingingBroadcastReceiver.NOTIFICATION_ID);
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void addFreeCoins(final UIConnectionResultCallback<Player> callback, final String handle) {
		try {
			final JSONObject top = JsonConstructor.addCoins(handle, getSession(), OS);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Player>(callback, ADD_FREE_COINS, Player.class).execute(top.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void addCoinsForAnOrder(final UIConnectionResultCallback<Player> callback, String handle, List<Order> orders) {
		try {
			final JSONObject top = JsonConstructor.addCoinsForAnOrder(handle, orders, getSession());
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Player>(callback, ADD_COINS_FOR_AN_ORDER, Player.class).execute(top
							.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void deleteConsumedOrders(final UIConnectionResultCallback<Player> callback, String handle,
			List<Order> orders) {
		try {
			final JSONObject top = JsonConstructor.deleteConsumedOrders(handle, orders, getSession());
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Player>(callback, DELETE_CONSUMED_ORDERS, Player.class).execute(top
							.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}

	}

	@Override
	public void invitePlayerForGame(final UIConnectionResultCallback<GameBoard> callback, String requesterHandle,
			String inviteeHandle, Long mapKey) {
		try {
			final JSONObject top = JsonConstructor.invite(requesterHandle, inviteeHandle, getSession(), mapKey);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<GameBoard>(callback, INVITE_USER_TO_PLAY, GameBoard.class).execute(top
							.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}

	}

	public void findGameById(final UIConnectionResultCallback<GameBoard> callback, String id, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);
		args.put("handle", handle);
		args.put("session", getSession());
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<GameBoard>(args, callback, FIND_GAME_BY_ID, GameBoard.class).execute("");
			}
		});
	}

	public void findCurrentGamesByPlayerHandle(final UIConnectionResultCallback<AvailableGames> callback, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		args.put("session", getSession());
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<AvailableGames>(args, callback, FIND_CURRENT_GAMES_BY_PLAYER_HANDLE,
						AvailableGames.class).execute("");
			}
		});
	}

	public void findGamesWithPendingMove(final UIConnectionResultCallback<GameCount> callback, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<GameCount>(args, callback, FIND_GAMES_WITH_A_PENDING_MOVE, GameCount.class)
						.execute("");
			}
		});
	}

	@Override
	public void findUserInformation(final UIConnectionResultCallback<Player> callback, String id, String authProvider) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);
		args.put("authProvider", authProvider);
		args.put("session", getSession());
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<Player>(args, callback, FIND_USER_BY_ID, Player.class).execute("");
			}
		});
	}

	@Override
	public void searchForPlayers(final UIConnectionResultCallback<People> callback, String searchTerm) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("searchTerm", searchTerm);
		args.put("session", getSession());
		args.put("handle", GameLoop.getUser().handle);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<People>(args, callback, SEARCH_FOR_USERS, People.class).execute("");
			}
		});

	}

	private GameActionCache<Inventory> inventoryCache = new GameActionCache<Inventory>(ONE_DAY_IN_MILLIS);

	@Override
	public void loadAvailableInventory(final UIConnectionResultCallback<Inventory> callback) {
		if (inventoryCache.getCache() != null) {
			callback.onConnectionResult(inventoryCache.getCache());
		} else {
			inventoryCache.setDelegate(callback);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new GetJsonRequestTask<Inventory>(new HashMap<String, String>(), inventoryCache,
							FIND_AVAILABLE_INVENTORY, Inventory.class).execute("");
				}
			});
		}
	}

	@Override
	public void findConfigByType(final UIConnectionResultCallback<Configuration> callback, final String type) {

		final Map<String, String> args = new HashMap<String, String>();
		args.put("type", type);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<Configuration>(args, callback, FIND_CONFIG_BY_TYPE, Configuration.class)
						.execute("");
			}
		});
	}

	public void requestHandleForId(final UIConnectionResultCallback<HandleResponse> callback, String id, String handle,
			String authProvider) {
		try {
			final JSONObject top = JsonConstructor.requestHandle(id, handle, getSession(), authProvider);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<HandleResponse>(callback, REQUEST_HANDLE_FOR_ID, HandleResponse.class)
							.execute(top.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	private class PostJsonRequestTask<T extends JsonConvertible> extends JsonRequestTask<T> {

		public PostJsonRequestTask(UIConnectionResultCallback<T> callback, String path, Class<T> converterClass) {
			super(callback, path, converterClass, null);
		}

		@Override
		public HttpURLConnection establishConnection(String... params) throws IOException {
			return Connection.establishPostConnection(config.getValue(PROTOCOL), config.getValue(HOST),
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
		public HttpURLConnection establishConnection(String... params) throws IOException {
			return Connection.establishGetConnection(config.getValue(PROTOCOL), config.getValue(HOST),
					config.getValue(PORT), path, args);
		}
	}

	private class RequestParams<T> {
		public Map<String, String> args;
		public String path;
		public Class<T> converter;
		public UIConnectionResultCallback<T> callback;
		public String[] params;
	}

	private abstract class JsonRequestTask<T extends JsonConvertible> extends AsyncTask<String, Void, JsonConvertible> {

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
				Log.e(TAG, "Could not create converter class", e);
			} catch (IllegalAccessException e) {
				Log.e(TAG, "Could not create converter class", e);
			}
			this.callback = callback;

			savedParams.path = path;
			savedParams.converter = converterClass;
			savedParams.callback = callback;
			savedParams.args = args;
		}

		public abstract HttpURLConnection establishConnection(String... params) throws IOException;

		@Override
		protected JsonConvertible doInBackground(String... params) {
			try {
				savedParams.params = params;
				Log.i(TAG, "Invoking call at path: " + path + ", " + Arrays.toString(params));
				return Connection.doRequest(connectivityManager, establishConnection(params), converter);
			} catch (IOException e) {
				Log.wtf(LOG_NAME, e);
				converter.errorMessage = CONNECTION_ERROR_MESSAGE;
			}

			return converter;
		}

		@Override
		protected void onPostExecute(final JsonConvertible result) {
			if (result.errorMessage != null && !result.errorMessage.isEmpty()) {
				Log.w(TAG, "Call failed with error: " + result.errorMessage);
				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						callback.onConnectionError(result.errorMessage);
					}
				});
			} else if (!result.valid) {
				callback.onConnectionError(result.reason);
			} else {
				if (result.sessionExpired) {
					Log.i(TAG, "Session expired, beginning silent sign in");
					Gdx.app.postRunnable(new Runnable() {
						public void run() {
							Preferences prefs = Gdx.app.getPreferences(GALCON_PREFS);
							prefs.putString(Constants.Auth.LAST_SESSION_ID, "");
							prefs.flush();

							socialAction.getToken(new SilentSignInAuthenticationListener<T>(savedParams));
						}
					});
				} else {
					Log.i(TAG, "Call succeeded");
					Gdx.app.postRunnable(new Runnable() {
						public void run() {
							callback.onConnectionResult((T) result);
						}
					});
				}
			}
		}
	}

	@Override
	public void showAd() {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) activity).displayAd();
			}
		});
	}

	@Override
	public void exchangeTokenForSession(final UIConnectionResultCallback<Session> callback, String authProvider,
			String token) {
		try {
			final JSONObject top = JsonConstructor.exchangeToken(authProvider, token);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Session>(callback, EXCHANGE_TOKEN_FOR_SESSION, Session.class).execute(top
							.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void resignGame(final UIConnectionResultCallback<GameBoard> callback, final String gameId, String handle) {
		try {
			final JSONObject top = JsonConstructor.resignGame(handle, getSession());

			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<GameBoard>(callback, RESIGN_GAME.replace(":gameId", gameId),
							GameBoard.class).execute(top.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void findFriends(final UIConnectionResultCallback<People> callback, final String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("session", getSession());
		args.put("handle", handle);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<People>(args, callback, FIND_FRIENDS, People.class).execute("");
			}
		});

	}

	@Override
	public void findPendingIvites(final UIConnectionResultCallback<GameQueue> callback, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("session", getSession());
		args.put("handle", handle);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<GameQueue>(args, callback, FIND_PENDING_INVITE, GameQueue.class).execute("");
			}
		});

	}

	@Override
	public void acceptInvite(final UIConnectionResultCallback<GameBoard> callback, String gameId, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		args.put("gameId", gameId);
		args.put("session", getSession());

		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<GameBoard>(args, callback, ACCEPT_INVITE, GameBoard.class).execute("");
			}
		});

	}

	@Override
	public void declineInvite(final UIConnectionResultCallback<BaseResult> callback, String gameId, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		args.put("gameId", gameId);
		args.put("session", getSession());

		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<BaseResult>(args, callback, DECLINE_INVITE, BaseResult.class).execute("");
			}
		});

	}

	@Override
	public void findMatchingFriends(final UIConnectionResultCallback<People> callback, List<String> authIds,
			String handle, String authProvider) {
		try {
			final JSONObject top = JsonConstructor.matchingFriends(authIds, handle, getSession(), authProvider);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<People>(callback, FIND_MATCHING_FRIENDS, People.class).execute(top
							.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	private Map<String, GameActionCache<Leaderboards>> friendLeaderboardCaches = new ConcurrentHashMap<String, GameActionCache<Leaderboards>>();

	@Override
	public void findLeaderboardsForFriends(final UIConnectionResultCallback<Leaderboards> callback,
			List<String> authIds, String handle, String authProvider) {

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
				activity.runOnUiThread(new Runnable() {
					public void run() {
						new PostJsonRequestTask<Leaderboards>(fCache, FIND_LEADERBOARDS_FOR_FRIENDS, Leaderboards.class)
								.execute(top.toString());
					}
				});
			} catch (JSONException e) {
				Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
			}
		}
	}

	@Override
	public void addProviderToUser(final UIConnectionResultCallback<PlayerList> callback, String handle, String id,
			String authProvider) {
		try {
			final JSONObject top = JsonConstructor.addProvider(handle, id, getSession(), authProvider);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<PlayerList>(callback, ADD_PROVIDER_TO_USER, PlayerList.class).execute(top
							.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void addProviderToUserWithOverride(final UIConnectionResultCallback<PlayerList> callback, String handle,
			String id, String authProvider, String keepSession, String deleteSession) {
		try {
			final JSONObject top = JsonConstructor.addProviderWithOverride(handle, id, getSession(), authProvider,
					keepSession, deleteSession);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<PlayerList>(callback, ADD_PROVIDER_TO_USER, PlayerList.class).execute(top
							.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void cancelGame(final UIConnectionResultCallback<BaseResult> callback, String handle, String gameId) {
		try {
			final JSONObject top = JsonConstructor.cancelGame(handle, gameId, session);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<BaseResult>(callback, CANCEL_GAME, BaseResult.class).execute(top.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void claimVictory(final UIConnectionResultCallback<GameBoard> callback, String handle, String gameId) {
		try {
			final JSONObject top = JsonConstructor.claimGame(handle, gameId, session);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<GameBoard>(callback, CLAIM_VICTORY, GameBoard.class).execute(top.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	private Map<String, GameActionCache<Leaderboards>> leaderboardCaches = new ConcurrentHashMap<String, GameActionCache<Leaderboards>>();

	@Override
	public void findLeaderboardById(final UIConnectionResultCallback<Leaderboards> callback, final String id) {
		GameActionCache<Leaderboards> leaderboardCache = leaderboardCaches.get(id);
		if (leaderboardCache == null) {
			leaderboardCache = new GameActionCache<Leaderboards>(ONE_HOUR_IN_MILLIS);
			leaderboardCaches.put(id, leaderboardCache);
		}

		if (leaderboardCache.getCache() != null) {
			callback.onConnectionResult(leaderboardCache.getCache());
		} else {
			leaderboardCache.setDelegate(callback);
			final GameActionCache<Leaderboards> cache = leaderboardCache;
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new GetJsonRequestTask<Leaderboards>(new HashMap<String, String>(), cache, FIND_LEADERBOARD_BY_ID
							.replace(":id", id), Leaderboards.class).execute("");
				}
			});
		}
	}

	public void practiceGame(final UIConnectionResultCallback<GameBoard> callback, String handle, Long mapId) {
		try {
			final JSONObject top = JsonConstructor.practiceGame(handle, session, mapId);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<GameBoard>(callback, PRACTICE, GameBoard.class).execute(top.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void shouldEnableAds(final boolean enable) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				activity.shouldEnableAds(enable);
			}
		});
	}
}

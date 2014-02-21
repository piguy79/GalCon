package com.xxx.galcon;

import static com.xxx.galcon.Config.HOST;
import static com.xxx.galcon.Config.PORT;
import static com.xxx.galcon.Config.PROTOCOL;
import static com.xxx.galcon.Constants.CONNECTION_ERROR_MESSAGE;
import static com.xxx.galcon.Constants.GALCON_PREFS;
import static com.xxx.galcon.MainActivity.LOG_NAME;
import static com.xxx.galcon.http.UrlConstants.ACCEPT_INVITE;
import static com.xxx.galcon.http.UrlConstants.ADD_COINS_FOR_AN_ORDER;
import static com.xxx.galcon.http.UrlConstants.ADD_FREE_COINS;
import static com.xxx.galcon.http.UrlConstants.DELETE_CONSUMED_ORDERS;
import static com.xxx.galcon.http.UrlConstants.EXCHANGE_TOKEN_FOR_SESSION;
import static com.xxx.galcon.http.UrlConstants.FIND_ALL_MAPS;
import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_GAMES;
import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_INVENTORY;
import static com.xxx.galcon.http.UrlConstants.FIND_CONFIG_BY_TYPE;
import static com.xxx.galcon.http.UrlConstants.FIND_CURRENT_GAMES_BY_PLAYER_HANDLE;
import static com.xxx.galcon.http.UrlConstants.FIND_FRIENDS;
import static com.xxx.galcon.http.UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE;
import static com.xxx.galcon.http.UrlConstants.FIND_GAME_BY_ID;
import static com.xxx.galcon.http.UrlConstants.FIND_MATCHING_FRIENDS;
import static com.xxx.galcon.http.UrlConstants.FIND_PENDING_INVITE;
import static com.xxx.galcon.http.UrlConstants.FIND_USER_BY_ID;
import static com.xxx.galcon.http.UrlConstants.INVITE_USER_TO_PLAY;
import static com.xxx.galcon.http.UrlConstants.JOIN_GAME;
import static com.xxx.galcon.http.UrlConstants.MATCH_PLAYER_TO_GAME;
import static com.xxx.galcon.http.UrlConstants.PERFORM_MOVES;
import static com.xxx.galcon.http.UrlConstants.RECOVER_USED_COINS_COUNT;
import static com.xxx.galcon.http.UrlConstants.REDUCE_TIME;
import static com.xxx.galcon.http.UrlConstants.REQUEST_HANDLE_FOR_ID;
import static com.xxx.galcon.http.UrlConstants.RESIGN_GAME;
import static com.xxx.galcon.http.UrlConstants.SEARCH_FOR_USERS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.crashlytics.android.Crashlytics;
import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.GameActionCache.InventoryCache;
import com.xxx.galcon.http.GameActionCache.MapsCache;
import com.xxx.galcon.http.JsonConstructor;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.BaseResult;
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
import com.xxx.galcon.service.PingService;

public class AndroidGameAction implements GameAction {
	private static final String TAG = "GameAction";
	private ConnectivityManager connectivityManager;
	private Activity activity;
	private SocialAction socialAction;
	private GameLoop gameLoop;
	private Config config = new AndroidConfig();

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

	public AndroidGameAction(Activity activity, SocialAction socialAction, ConnectivityManager connectivityManager) {
		this.connectivityManager = connectivityManager;
		this.activity = activity;
		this.socialAction = socialAction;
	}

	public void findAvailableGames(final UIConnectionResultCallback<AvailableGames> callback, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);

		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<AvailableGames>(args, callback, FIND_AVAILABLE_GAMES, AvailableGames.class)
						.execute("");
			}
		});
	}

	private MapsCache mapCache = new MapsCache();

	@Override
	public void findAllMaps(final UIConnectionResultCallback<Maps> callback) {
		if (mapCache.getCache() != null) {
			callback.onConnectionResult(mapCache.getCache());
		} else {
			mapCache.setDelegate(callback);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new GetJsonRequestTask<Maps>(new HashMap<String, String>(), mapCache, FIND_ALL_MAPS, Maps.class)
							.execute("");
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
					mNotificationManager.cancel(PingService.NOTIFICATION_ID);
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	@Override
	public void addFreeCoins(final UIConnectionResultCallback<Player> callback, final String handle) {
		try {
			final JSONObject top = JsonConstructor.addCoins(handle, getSession());
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
			final JSONObject top = JsonConstructor.deleteConsumedOrders(handle, orders);
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
	public void reduceTimeUntilNextGame(final UIConnectionResultCallback<Player> callback, final String handle) {
		try {
			final JSONObject top = JsonConstructor.reduceCall(handle, getSession());
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Player>(callback, REDUCE_TIME, Player.class).execute(top.toString());
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

	public void findGamesWithPendingMove(final UIConnectionResultCallback<AvailableGames> callback, String handle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", handle);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<AvailableGames>(args, callback, FIND_GAMES_WITH_A_PENDING_MOVE,
						AvailableGames.class).execute("");
			}
		});
	}

	@Override
	public void findUserInformation(final UIConnectionResultCallback<Player> callback, String id) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);
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
		args.put("handle", GameLoop.USER.handle);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<People>(args, callback, SEARCH_FOR_USERS, People.class).execute("");
			}
		});

	}

	private InventoryCache inventoryCache = new InventoryCache();

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

	public void requestHandleForId(final UIConnectionResultCallback<HandleResponse> callback, String id, String handle) {
		try {
			final JSONObject top = JsonConstructor.requestHandle(id, handle, getSession());
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
	public void showAd(final AdColonyVideoListener listener) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) activity).displayAd(listener);
			}
		});
	}

	@Override
	public void recoverUsedCoinCount(final UIConnectionResultCallback<Player> callback, String handle) {
		try {
			final JSONObject top = JsonConstructor.user(handle, getSession());
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Player>(callback, RECOVER_USED_COINS_COUNT, Player.class).execute(top
							.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
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
				new GetJsonRequestTask<BaseResult>(args, callback, ACCEPT_INVITE, BaseResult.class).execute("");
			}
		});

	}

	@Override
	public void findMatchingFriends(final UIConnectionResultCallback<People> callback, List<String> authIds,
			String handle) {
		try {
			final JSONObject top = JsonConstructor.matchingFriends(authIds, handle, getSession());
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

}

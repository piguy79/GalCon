package com.xxx.galcon;

import static com.xxx.galcon.Config.HOST;
import static com.xxx.galcon.Config.PORT;
import static com.xxx.galcon.Constants.CONNECTION_ERROR_MESSAGE;
import static com.xxx.galcon.Constants.GALCON_PREFS;
import static com.xxx.galcon.MainActivity.LOG_NAME;
import static com.xxx.galcon.http.UrlConstants.ADD_COINS;
import static com.xxx.galcon.http.UrlConstants.ADD_COINS_FOR_AN_ORDER;
import static com.xxx.galcon.http.UrlConstants.DELETE_CONSUMED_ORDERS;
import static com.xxx.galcon.http.UrlConstants.EXCHANGE_TOKEN_FOR_SESSION;
import static com.xxx.galcon.http.UrlConstants.FIND_ALL_MAPS;
import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_GAMES;
import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_INVENTORY;
import static com.xxx.galcon.http.UrlConstants.FIND_CONFIG_BY_TYPE;
import static com.xxx.galcon.http.UrlConstants.FIND_CURRENT_GAMES_BY_PLAYER_HANDLE;
import static com.xxx.galcon.http.UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE;
import static com.xxx.galcon.http.UrlConstants.FIND_GAME_BY_ID;
import static com.xxx.galcon.http.UrlConstants.FIND_USER_BY_EMAIL;
import static com.xxx.galcon.http.UrlConstants.JOIN_GAME;
import static com.xxx.galcon.http.UrlConstants.MATCH_PLAYER_TO_GAME;
import static com.xxx.galcon.http.UrlConstants.PERFORM_MOVES;
import static com.xxx.galcon.http.UrlConstants.RECOVER_USED_COINS_COUNT;
import static com.xxx.galcon.http.UrlConstants.REDUCE_TIME;
import static com.xxx.galcon.http.UrlConstants.REQUEST_HANDLE_FOR_EMAIL;

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
import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.AndroidGameActionCache.MapsCache;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.AuthenticationListener;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.JsonConstructor;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.inappbilling.util.StoreResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Order;
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
			Log.i(TAG, "Silent sign in succeeded.  Getting token...");
			AndroidGameAction.this.exchangeTokenForSession(new UIConnectionResultCallback<Session>() {

				@Override
				public void onConnectionResult(Session result) {
					Log.i(TAG, "Silent sign in succeeded.  Token retrieved.");
					if (savedRequestParams.args != null) {
						new GetJsonRequestTask<T>(savedRequestParams.args, savedRequestParams.callback,
								savedRequestParams.path, savedRequestParams.converter).execute("");
					} else {
						new PostJsonRequestTask<T>(savedRequestParams.callback, savedRequestParams.path,
								savedRequestParams.converter).execute(savedRequestParams.params);
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
	public void setSession(String session) {
		this.session = session;
	}

	public void setGameLoop(GameLoop gameLoop) {
		this.gameLoop = gameLoop;
	}

	public AndroidGameAction(Activity activity, SocialAction socialAction, ConnectivityManager connectivityManager) {
		this.connectivityManager = connectivityManager;
		this.activity = activity;
		this.socialAction = socialAction;
	}

	public void findAvailableGames(final UIConnectionResultCallback<AvailableGames> callback, String playerHandle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);

		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<AvailableGames>(args, callback, FIND_AVAILABLE_GAMES, new AvailableGames())
						.execute("");
			}
		});
	}

	private MapsCache mapCache = new MapsCache();

	@Override
	public void findAllMaps(final UIConnectionResultCallback<Maps> callback) {
		if (mapCache.getCachedMaps() != null) {
			callback.onConnectionResult(mapCache.getCachedMaps());
		} else {
			mapCache.setDelegate(callback);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new GetJsonRequestTask<Maps>(new HashMap<String, String>(), mapCache, FIND_ALL_MAPS, new Maps())
							.execute("");
				}
			});
		}
	}

	public void joinGame(final UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);
		args.put("id", id);

		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<GameBoard>(args, callback, JOIN_GAME, new GameBoard()).execute("");
			}
		});
	}

	@Override
	public void matchPlayerToGame(final UIConnectionResultCallback<GameBoard> callback, String playerHandle,
			Long mapToFind) {
		try {

			final JSONObject top = JsonConstructor.matchPlayerToGame(playerHandle, mapToFind);

			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<GameBoard>(callback, MATCH_PLAYER_TO_GAME, new GameBoard()).execute(top
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
			final JSONObject top = JsonConstructor.performMove(gameId, moves, harvestMoves);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<GameBoard>(callback, PERFORM_MOVES, new GameBoard()).execute(top.toString());
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
	public void addCoins(final UIConnectionResultCallback<Player> callback, final String playerHandle,
			final int numCoins) {
		try {
			final JSONObject top = JsonConstructor.addCoins(playerHandle, numCoins);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Player>(callback, ADD_COINS, new Player()).execute(top.toString());
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
	public void addCoinsForAnOrder(final UIConnectionResultCallback<Player> callback, String playerHandle,
			List<Order> orders) throws ConnectionException {
		try {
			final JSONObject top = JsonConstructor.addCoinsForAnOrder(playerHandle, orders);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Player>(callback, ADD_COINS_FOR_AN_ORDER, new Player()).execute(top
							.toString());
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
	public void deleteConsumedOrders(final UIConnectionResultCallback<Player> callback, String playerHandle,
			List<Order> orders) {
		try {
			final JSONObject top = JsonConstructor.deleteConsumedOrders(playerHandle, orders);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Player>(callback, DELETE_CONSUMED_ORDERS, new Player()).execute(top
							.toString());
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
	public void reduceTimeUntilNextGame(final UIConnectionResultCallback<Player> callback, final String playerHandle,
			Long timeRemaining, Long usedCoins) {
		try {
			final JSONObject top = JsonConstructor.reduceCall(playerHandle, timeRemaining, usedCoins);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Player>(callback, REDUCE_TIME, new Player()).execute(top.toString());
					NotificationManager mNotificationManager = (NotificationManager) activity
							.getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancel(PingService.NOTIFICATION_ID);
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	public void findGameById(final UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);
		args.put("playerHandle", playerHandle);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<GameBoard>(args, callback, FIND_GAME_BY_ID, new GameBoard()).execute("");
			}
		});
	}

	public void findCurrentGamesByPlayerHandle(final UIConnectionResultCallback<AvailableGames> callback,
			String playerHandle) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("handle", playerHandle);
		args.put("session", getSession());
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<AvailableGames>(args, callback, FIND_CURRENT_GAMES_BY_PLAYER_HANDLE,
						new AvailableGames()).execute("");
			}
		});
	}

	public void findGamesWithPendingMove(final UIConnectionResultCallback<AvailableGames> callback, String playerHandle)
			throws ConnectionException {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<AvailableGames>(args, callback, FIND_GAMES_WITH_A_PENDING_MOVE,
						new AvailableGames()).execute("");
			}
		});
	}

	@Override
	public void findUserInformation(final UIConnectionResultCallback<Player> callback, String email) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("email", email);
		args.put("session", session);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<Player>(args, callback, FIND_USER_BY_EMAIL, new Player()).execute("");
			}
		});
	}

	@Override
	public void loadAvailableInventory(final UIConnectionResultCallback<Inventory> callback) {
		final Map<String, String> args = new HashMap<String, String>();

		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<Inventory>(args, callback, FIND_AVAILABLE_INVENTORY, new Inventory())
						.execute("");
			}
		});
	}

	@Override
	public void findConfigByType(final UIConnectionResultCallback<Configuration> callback, final String type) {

		final Map<String, String> args = new HashMap<String, String>();
		args.put("type", type);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<Configuration>(args, callback, FIND_CONFIG_BY_TYPE, new Configuration())
						.execute("");
			}
		});
	}

	public void requestHandleForEmail(final UIConnectionResultCallback<HandleResponse> callback, String email,
			String handle) {
		try {
			final JSONObject top = JsonConstructor.requestHandle(email, handle, getSession());
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<HandleResponse>(callback, REQUEST_HANDLE_FOR_EMAIL, new HandleResponse())
							.execute(top.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}

	private class PostJsonRequestTask<T extends JsonConvertible> extends JsonRequestTask<T> {

		public PostJsonRequestTask(UIConnectionResultCallback<T> callback, String path, JsonConvertible converter) {
			super(callback, path, converter, null);
		}

		@Override
		public HttpURLConnection establishConnection(String... params) throws IOException {
			return Connection.establishPostConnection(Config.getValue(HOST), Config.getValue(PORT), path, params);
		}
	}

	private class GetJsonRequestTask<T extends JsonConvertible> extends JsonRequestTask<T> {
		private Map<String, String> args;

		public GetJsonRequestTask(Map<String, String> args, UIConnectionResultCallback<T> callback, String path,
				JsonConvertible converter) {
			super(callback, path, converter, args);
			this.args = args;
		}

		@Override
		public HttpURLConnection establishConnection(String... params) throws IOException {
			return Connection.establishGetConnection(Config.getValue(HOST), Config.getValue(PORT), path, args);
		}
	}

	private class RequestParams<T> {
		public Map<String, String> args;
		public String path;
		public JsonConvertible converter;
		public UIConnectionResultCallback<T> callback;
		public String[] params;
	}

	private abstract class JsonRequestTask<T extends JsonConvertible> extends AsyncTask<String, Void, JsonConvertible> {

		protected String path;
		private JsonConvertible converter;
		private UIConnectionResultCallback<T> callback;

		private RequestParams<T> savedParams = new RequestParams<T>();

		public JsonRequestTask(UIConnectionResultCallback<T> callback, String path, JsonConvertible converter,
				Map<String, String> args) {
			this.path = path;
			this.converter = converter;
			this.callback = callback;

			savedParams.path = path;
			savedParams.converter = converter;
			savedParams.callback = callback;
			savedParams.args = args;
		}

		public abstract HttpURLConnection establishConnection(String... params) throws IOException;

		@Override
		protected JsonConvertible doInBackground(String... params) {
			try {
				savedParams.params = params;
				Log.i(TAG, "Invoking call: " + Arrays.toString(params));
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
	public void purchaseCoins(final InventoryItem inventoryItem, final UIConnectionResultCallback<Player> callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) activity).purchaseCoins(inventoryItem, callback);
			}
		});
	}

	@Override
	public void loadStoreInventory(final Inventory inventory, final StoreResultCallback<Inventory> callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) activity).loadInventory(inventory, callback);
			}
		});

	}

	@Override
	public void consumeOrders(final List<Order> orders) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) activity).consumeOrders(orders);
			}
		});
	}

	@Override
	public void consumeExistingOrders() {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) activity).setupInAppBilling();
			}
		});
	}

	@Override
	public void recoverUsedCoinCount(final UIConnectionResultCallback<Player> callback, String playerHandle) {
		try {
			final JSONObject top = JsonConstructor.userWithTime(playerHandle);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Player>(callback, RECOVER_USED_COINS_COUNT, new Player()).execute(top
							.toString());
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
	public void exchangeTokenForSession(final UIConnectionResultCallback<Session> callback, String authProvider,
			String token) {
		try {
			final JSONObject top = JsonConstructor.exchangeToken(authProvider, token);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<Session>(callback, EXCHANGE_TOKEN_FOR_SESSION, new Session()).execute(top
							.toString());
				}
			});
		} catch (JSONException e) {
			Log.wtf(LOG_NAME, "This isn't expected to ever realistically happen. So I'm just logging it.");
		}
	}
}

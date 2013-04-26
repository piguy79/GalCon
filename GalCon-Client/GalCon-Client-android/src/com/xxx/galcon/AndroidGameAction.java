package com.xxx.galcon;

import static com.xxx.galcon.Config.HOST;
import static com.xxx.galcon.Config.PORT;
import static com.xxx.galcon.http.UrlConstants.FIND_ACTIVE_GAMES_FOR_A_USER;
import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_GAMES;
import static com.xxx.galcon.http.UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE;
import static com.xxx.galcon.http.UrlConstants.FIND_GAME_BY_ID;
import static com.xxx.galcon.http.UrlConstants.FIND_USER_BY_USER_NAME;
import static com.xxx.galcon.http.UrlConstants.GENERATE_GAME;
import static com.xxx.galcon.http.UrlConstants.JOIN_GAME;
import static com.xxx.galcon.http.UrlConstants.PERFORM_MOVES;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.os.AsyncTask;

import com.badlogic.gdx.Gdx;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.JsonConstructor;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.base.JsonConvertible;

public class AndroidGameAction implements GameAction {
	private ConnectivityManager connectivityManager;
	private Activity activity;

	public AndroidGameAction(Activity activity, ConnectivityManager connectivityManager) {
		this.connectivityManager = connectivityManager;
		this.activity = activity;
	}

	public void findAvailableGames(final UIConnectionResultCallback<AvailableGames> callback, String player) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("player", player);

		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<AvailableGames>(args, callback, FIND_AVAILABLE_GAMES, new AvailableGames())
						.execute("");
			}
		});
	}

	public void joinGame(final UIConnectionResultCallback<GameBoard> callback, String id, String player) {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("player", player);
		args.put("id", id);

		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<GameBoard>(args, callback, JOIN_GAME, new GameBoard()).execute("");
			}
		});
	}

	public void generateGame(final UIConnectionResultCallback<GameBoard> callback, String player, int width, int height)
			throws ConnectionException {
		try {
			final JSONObject top = JsonConstructor.generateGame(player, width, height);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<GameBoard>(callback, GENERATE_GAME, new GameBoard()).execute(top.toString());
				}
			});
		} catch (JSONException e) {
			throw new ConnectionException(e);
		}
	}

	public void performMoves(final UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves)
			throws ConnectionException {
		try {
			final JSONObject top = JsonConstructor.performMove(gameId, moves);
			activity.runOnUiThread(new Runnable() {
				public void run() {
					new PostJsonRequestTask<GameBoard>(callback, PERFORM_MOVES, new GameBoard()).execute(top.toString());
				}
			});
		} catch (JSONException e) {
			throw new ConnectionException(e);
		}
	}

	public void findGameById(final UIConnectionResultCallback<GameBoard> callback, String id)
			throws ConnectionException {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<GameBoard>(args, callback, FIND_GAME_BY_ID, new GameBoard()).execute("");
			}
		});
	}

	public void findActiveGamesForAUser(final UIConnectionResultCallback<AvailableGames> callback, String player)
			throws ConnectionException {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("userName", player);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<AvailableGames>(args, callback, FIND_ACTIVE_GAMES_FOR_A_USER,
						new AvailableGames()).execute("");
			}
		});
	}

	public void findGamesWithPendingMove(final UIConnectionResultCallback<AvailableGames> callback, String player)
			throws ConnectionException {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("userName", player);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<AvailableGames>(args, callback, FIND_GAMES_WITH_A_PENDING_MOVE,
						new AvailableGames()).execute("");
			}
		});
	}

	private class PostJsonRequestTask<T extends JsonConvertible> extends JsonRequestTask<T> {

		public PostJsonRequestTask(UIConnectionResultCallback<T> callback, String path, JsonConvertible converter) {
			super(callback, path, converter);
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
			super(callback, path, converter);
			this.args = args;
		}

		@Override
		public HttpURLConnection establishConnection(String... params) throws IOException {
			return Connection.establishGetConnection(Config.getValue(HOST), Config.getValue(PORT), path, args);
		}
	}

	private abstract class JsonRequestTask<T extends JsonConvertible> extends AsyncTask<String, Void, JsonConvertible> {

		protected String path;
		private JsonConvertible converter;
		private UIConnectionResultCallback<T> callback;
		private String error = null;

		public JsonRequestTask(UIConnectionResultCallback<T> callback, String path, JsonConvertible converter) {
			this.path = path;
			this.converter = converter;
			this.callback = callback;
		}

		public abstract HttpURLConnection establishConnection(String... params) throws IOException;

		@Override
		protected JsonConvertible doInBackground(String... params) {
			try {
				return Connection.doRequest(connectivityManager, establishConnection(params), converter);
			} catch (IOException e) {
				error = e.getMessage();
			}

			return null;
		}

		@Override
		protected void onPostExecute(final JsonConvertible result) {
			if (error == null) {
				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						callback.onConnectionResult((T) result);
					}
				});
			} else {
				Gdx.app.postRunnable(new Runnable() {
					public void run() {
						callback.onConnectionError(error);
					}
				});
			}
		}
	}

	public void findUserInformation(final UIConnectionResultCallback<Player> callback, String player)
			throws ConnectionException {
		final Map<String, String> args = new HashMap<String, String>();
		args.put("userName", player);
		activity.runOnUiThread(new Runnable() {
			public void run() {
				new GetJsonRequestTask<Player>(args, callback, FIND_USER_BY_USER_NAME, new Player()).execute("");
			}
		});

	}
}

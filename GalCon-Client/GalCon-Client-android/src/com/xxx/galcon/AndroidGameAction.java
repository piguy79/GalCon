package com.xxx.galcon;

import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_GAMES;
import static com.xxx.galcon.http.UrlConstants.FIND_GAME_BY_ID;
import static com.xxx.galcon.http.UrlConstants.GENERATE_GAME;
import static com.xxx.galcon.http.UrlConstants.JOIN_GAME;
import static com.xxx.galcon.http.UrlConstants.PERFORM_MOVES;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.ConnectionResultCallback;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.JsonConstructor;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.base.JsonConvertible;

public class AndroidGameAction implements GameAction {
	public static final int CONNECTION_TIMEOUT = 20000;

	private String host;
	private String port;
	private ConnectivityManager connectivityManager;

	public AndroidGameAction(ConnectivityManager connectivityManager, String host, String port) {
		this.host = host;
		this.port = port;
		this.connectivityManager = connectivityManager;
	}

	public void findAvailableGames(ConnectionResultCallback<AvailableGames> callback) {
		Map<String, String> args = new HashMap<String, String>();

		new GetJsonRequestTask<AvailableGames>(args, callback, FIND_AVAILABLE_GAMES, new AvailableGames()).execute("");
	}

	public void joinGame(ConnectionResultCallback<GameBoard> callback, String id, String player) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("player", player);
		args.put("id", id);

		new GetJsonRequestTask<GameBoard>(args, callback, JOIN_GAME, new GameBoard()).execute("");
	}

	public void generateGame(ConnectionResultCallback<GameBoard> callback, String player, int width, int height)
			throws ConnectionException {
		try {
			JSONObject top = JsonConstructor.generateGame(player, width, height);
			new PostJsonRequestTask<GameBoard>(callback, GENERATE_GAME, new GameBoard()).execute(top.toString());
		} catch (JSONException e) {
			throw new ConnectionException(e);
		}
	}

	public void performMoves(ConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves)
			throws ConnectionException {
		try {
			JSONObject top = JsonConstructor.performMove(gameId, moves);
			new PostJsonRequestTask<GameBoard>(callback, PERFORM_MOVES, new GameBoard()).execute(top.toString());
		} catch (JSONException e) {
			throw new ConnectionException(e);
		}
	}

	public void findGameById(ConnectionResultCallback<GameBoard> callback, String id) throws ConnectionException {
		Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);

		new GetJsonRequestTask<GameBoard>(args, callback, FIND_GAME_BY_ID, new GameBoard()).execute("");
	}

	private class PostJsonRequestTask<T extends JsonConvertible> extends JsonRequestTask<T> {

		public PostJsonRequestTask(ConnectionResultCallback<T> callback, String path, JsonConvertible converter) {
			super(callback, path, converter);
		}

		@Override
		public HttpURLConnection establishConnection(String... params) throws IOException {
			URL url = new URL("http://" + AndroidGameAction.this.host + ":" + AndroidGameAction.this.port + path);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestMethod("POST");
			connection.connect();

			OutputStream os = connection.getOutputStream();
			os.write(params[0].getBytes("UTF-8"));
			os.close();

			return connection;
		}
	}

	private class GetJsonRequestTask<T extends JsonConvertible> extends JsonRequestTask<T> {
		private Map<String, String> args;

		public GetJsonRequestTask(Map<String, String> args, ConnectionResultCallback<T> callback, String path,
				JsonConvertible converter) {
			super(callback, path, converter);
			this.args = args;
		}

		@Override
		public HttpURLConnection establishConnection(String... params) throws IOException {
			StringBuilder sb = new StringBuilder("?");

			for (Map.Entry<String, String> arg : args.entrySet()) {
				sb.append(arg.getKey()).append("=").append(arg.getValue()).append("&");
			}

			URL url = new URL("http://" + AndroidGameAction.this.host + ":" + AndroidGameAction.this.port + path
					+ sb.toString());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.setRequestMethod("GET");
			connection.connect();

			return connection;
		}
	}

	private abstract class JsonRequestTask<T extends JsonConvertible> extends AsyncTask<String, Void, JsonConvertible> {

		protected String path;
		private JsonConvertible converter;
		private ConnectionResultCallback<T> callback;

		public JsonRequestTask(ConnectionResultCallback<T> callback, String path, JsonConvertible converter) {
			this.path = path;
			this.converter = converter;
			this.callback = callback;
		}

		public abstract HttpURLConnection establishConnection(String... params) throws IOException;

		@Override
		protected JsonConvertible doInBackground(String... params) {
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			if (networkInfo == null || !networkInfo.isConnected()) {
				return null;
			}

			HttpURLConnection connection = null;
			try {
				connection = establishConnection(params);

				StringBuilder sb = new StringBuilder();
				InputStreamReader reader = new InputStreamReader(connection.getInputStream());
				char[] buffer = new char[0x1000];
				int read = 0;
				while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
					sb.append(buffer, 0, read);
				}
				reader.close();

				converter.consume(new JSONObject(sb.toString()));
			} catch (MalformedURLException e) {
				Log.wtf("error", "error", e);
			} catch (IOException e) {
				Log.wtf("error", "error", e);
			} catch (JSONException e) {
				Log.wtf("error", "error", e);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}

			return converter;
		}

		@Override
		protected void onPostExecute(JsonConvertible result) {
			callback.result((T) result);
		}
	}
}

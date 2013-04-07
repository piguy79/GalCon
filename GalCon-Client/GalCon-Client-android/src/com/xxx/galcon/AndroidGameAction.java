package com.xxx.galcon;

import static com.xxx.galcon.http.UrlConstants.FIND_GAME_BY_ID;
import static com.xxx.galcon.http.UrlConstants.GENERATE_GAME;
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

	public AvailableGames findAvailableGames() {
		// return (AvailableGames) callURL(new GetClientRequest(),
		// FIND_AVAILABLE_GAMES, new HashMap<String, String>(),
		// new AvailableGames());
		return null;
	}

	public GameBoard joinGame(String id, String player) {
		// TODO Auto-generated method stub
		return null;
	}

	public void generateGame(ConnectionResultCallback<GameBoard> callback, String player, int width, int height)
			throws ConnectionException {
		try {
			JSONObject top = JsonConstructor.generateGame(player, width, height);
			new PostJsonRequestTask(callback, GENERATE_GAME, new GameBoard()).execute(top.toString());
		} catch (JSONException e) {
			throw new ConnectionException(e);
		}
	}

	public void performMoves(ConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves)
			throws ConnectionException {
		try {
			JSONObject top = JsonConstructor.performMove(gameId, moves);
			new PostJsonRequestTask(callback, PERFORM_MOVES, new GameBoard()).execute(top.toString());
		} catch (JSONException e) {
			throw new ConnectionException(e);
		}
	}

	public void findGameById(ConnectionResultCallback<GameBoard> callback, String id) throws ConnectionException {
		Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);
		new GetJsonRequestTask(args, callback, FIND_GAME_BY_ID, new GameBoard()).execute("");
	}

	private class PostJsonRequestTask extends JsonRequestTask {

		public PostJsonRequestTask(ConnectionResultCallback<GameBoard> callback, String path, JsonConvertible converter) {
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

	private class GetJsonRequestTask extends JsonRequestTask {
		private Map<String, String> args;

		public GetJsonRequestTask(Map<String, String> args, ConnectionResultCallback<GameBoard> callback, String path,
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

	private abstract class JsonRequestTask extends AsyncTask<String, Void, JsonConvertible> {

		protected String path;
		private JsonConvertible converter;
		private ConnectionResultCallback<GameBoard> callback;

		public JsonRequestTask(ConnectionResultCallback<GameBoard> callback, String path, JsonConvertible converter) {
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
			callback.result((GameBoard) result);
		}
	}
}

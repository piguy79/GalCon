package com.xxx.galcon;

import static com.xxx.galcon.http.UrlConstants.GENERATE_GAME;

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
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.base.JsonConvertible;

public class AndroidGameAction implements GameAction {

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
			JSONObject top = new JSONObject();

			top.put("player", player);
			top.put("width", width);
			top.put("height", height);

			Map<String, String> args = new HashMap<String, String>();

			args.put("json", top.toString());
			callURL(callback, GENERATE_GAME, args, new GameBoard());

		} catch (JSONException e) {
			throw new ConnectionException(e);
		}
	}

	public GameBoard performMoves(String gameId, List<Move> moves) throws ConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	public GameBoard findGameById(String id) throws ConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	private void callURL(ConnectionResultCallback<GameBoard> callback, String path, Map<String, String> parameters,
			JsonConvertible converter) throws ConnectionException {

		JsonRequestTask task = new JsonRequestTask(callback, path, converter);
		task.execute(parameters.get("json"));
	}

	private class JsonRequestTask extends AsyncTask<String, Void, JsonConvertible> {

		private String path;
		private JsonConvertible converter;
		private ConnectionResultCallback<GameBoard> callback;

		public JsonRequestTask(ConnectionResultCallback<GameBoard> callback, String path, JsonConvertible converter) {
			this.path = path;
			this.converter = converter;
			this.callback = callback;
		}

		@Override
		protected JsonConvertible doInBackground(String... params) {
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			if (networkInfo == null || !networkInfo.isConnected()) {
				return null;
			}

			try {
				URL url = new URL("http://" + AndroidGameAction.this.host + ":" + AndroidGameAction.this.port + path);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(20000);
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Accept", "application/json");
				connection.setRequestMethod("POST");
				connection.connect();

				byte[] outputBytes = params[0].getBytes("UTF-8");
				OutputStream os = connection.getOutputStream();
				os.write(outputBytes);
				os.close();

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
			}

			return converter;
		}

		@Override
		protected void onPostExecute(JsonConvertible result) {
			callback.result((GameBoard) result);
		}
	}
}

package com.xxx.galcon;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.xxx.galcon.model.base.JsonConvertible;

public class Connection {
	public static final int CONNECTION_TIMEOUT = 20000;

	public static HttpURLConnection establishGetConnection(String host, String port, String path,
			Map<String, String> args) throws IOException {
		StringBuilder sb = new StringBuilder("?");

		for (Map.Entry<String, String> arg : args.entrySet()) {
			sb.append(arg.getKey()).append("=").append(arg.getValue()).append("&");
		}

		URL url = new URL("http://" + host + ":" + port + path + sb.toString());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(CONNECTION_TIMEOUT);
		connection.setRequestMethod("GET");
		connection.connect();

		return connection;
	}

	public static HttpURLConnection establishPostConnection(String host, String port, String path, String... params)
			throws IOException {
		URL url = new URL("http://" + host + ":" + port + path);
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

	public static JsonConvertible doRequest(ConnectivityManager connectivityManager, HttpURLConnection connection,
			JsonConvertible converter) {
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected()) {
			return null;
		}

		try {
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
}
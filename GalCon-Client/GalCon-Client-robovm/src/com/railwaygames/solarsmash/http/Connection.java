package com.railwaygames.solarsmash.http;

import static com.railwaygames.solarsmash.Constants.CONNECTION_ERROR_MESSAGE;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.json.JSONObject;

import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class Connection {
	public static final int CONNECTION_TIMEOUT = 10000;

	public static HttpURLConnection establishGetConnection(String protocol, String host, String port, String path,
			Map<String, String> args) throws IOException {
		StringBuilder sb = new StringBuilder("?");

		for (Map.Entry<String, String> arg : args.entrySet()) {
			sb.append(arg.getKey()).append("=").append(arg.getValue()).append("&");
		}

		URL url = new URL(protocol + "://" + host + ":" + port + path + sb.toString());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(CONNECTION_TIMEOUT);
		connection.setRequestMethod("GET");
		connection.connect();

		return connection;
	}

	public static HttpURLConnection establishPostConnection(String protocol, String host, String port, String path,
			String... params) throws IOException {
		URL url = new URL(protocol + "://" + host + ":" + port + path);
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

	public static JsonConvertible doRequest(HttpURLConnection connection, JsonConvertible converter) {
		try {
			StringBuilder sb = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(connection.getInputStream());
			char[] buffer = new char[0x1000];
			int read = 0;
			while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
				sb.append(buffer, 0, read);
			}
			reader.close();

			JSONObject returnObject = new JSONObject(sb.toString());
			String errorOccurred = returnObject.optString("error");
			if (errorOccurred != null && errorOccurred.trim().length() > 0) {
				converter.errorMessage = errorOccurred;
			} else {
				converter.consume(new JSONObject(sb.toString()));
			}
		} catch (Exception e) {
			converter.errorMessage = CONNECTION_ERROR_MESSAGE;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return converter;
	}
}
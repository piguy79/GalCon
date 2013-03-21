/**
 * 
 */
package com.xxx.galcon.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.base.JsonConvertible;

/**
 * This is a Apache Commons implementation of the GameAction interface.
 * 
 * @author conormullen
 * 
 */
/**
 * @author conormullen
 * 
 */
public class DesktopGameAction extends BaseDesktopGameAction implements GameAction {

	public DesktopGameAction(String host, int port) {
		super(host, port);
	}

	@Override
	public AvailableGames findAllGames() {
		return (AvailableGames) callURL("/findAllGames", new HashMap<String, String>(), new AvailableGames());
	}

	@Override
	public GameBoard generateGame(String player) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("player", player);
		return (GameBoard) callURL("/generateGame", args, new GameBoard());
	}

	@Override
	public GameBoard joinGame(String id, String player) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("player", player);
		args.put("id", id);
		return (GameBoard) callURL("/joinGame", args, new GameBoard());
	}

	private JsonConvertible callURL(String path, Map<String, String> parameters, JsonConvertible converter) {
		try {
			String postResponse = executeHttpRequest(path, parameters);
			return buildObjectsFromResponse(converter, postResponse);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * This method us used to populate a Gameboard Object with attributes form a
	 * JsonObject.
	 * 
	 * @param gameBoard
	 * @param postResponse
	 * @return
	 */
	private JsonConvertible buildObjectsFromResponse(JsonConvertible converter, String postResponse) {
		System.out.println(postResponse);

		try {
			JSONObject gameInformation = new JSONObject(postResponse);

			converter.consume(gameInformation);

			return converter;

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}

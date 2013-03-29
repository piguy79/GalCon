/**
 * 
 */
package com.xxx.galcon.http;

import static com.xxx.galcon.http.UrlConstants.FIND_ALL_GAMES;
import static com.xxx.galcon.http.UrlConstants.GENERATE_GAME;
import static com.xxx.galcon.http.UrlConstants.JOIN_GAME;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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
public class DesktopGameAction extends BaseDesktopGameAction implements GameAction {

	public DesktopGameAction(String host, int port) {
		super(host, port);
	}

	@Override
	public AvailableGames findAllGames() throws ConnectionException {
		return (AvailableGames) callURL(GET, FIND_ALL_GAMES, new HashMap<String, String>(), new AvailableGames());
	}

	@Override
	public GameBoard generateGame(String player, int width, int height) throws ConnectionException {
		Map<String, String> args = new HashMap<String, String>();
		args.put("player", player);
		args.put("width", Integer.toString(width));
		args.put("height", Integer.toString(height));
		return (GameBoard) callURL(POST, GENERATE_GAME, args, new GameBoard());
	}

	@Override
	public GameBoard joinGame(String id, String player) throws ConnectionException {
		Map<String, String> args = new HashMap<String, String>();
		args.put("player", player);
		args.put("id", id);
		return (GameBoard) callURL(GET, JOIN_GAME, args, new GameBoard());
	}

	private JsonConvertible callURL(String method, String path, Map<String, String> parameters,
			JsonConvertible converter) throws ConnectionException {
		try {
			String postResponse = executeHttpRequest(method, path, parameters);
			return buildObjectsFromResponse(converter, postResponse);
		} catch (MalformedURLException e) {
			throw new ConnectionException(e);
		} catch (IOException e) {
			throw new ConnectionException(e);
		} catch (URISyntaxException e) {
			throw new ConnectionException(e);
		}
	}

	/**
	 * This method us used to populate a Gameboard Object with attributes form a
	 * JsonObject.
	 * 
	 * @param gameBoard
	 * @param postResponse
	 * @return
	 */
	private JsonConvertible buildObjectsFromResponse(JsonConvertible converter, String postResponse)
			throws ConnectionException {
		System.out.println(postResponse);

		try {
			JSONObject gameInformation = new JSONObject(postResponse);
			converter.consume(gameInformation);

			return converter;

		} catch (JSONException e) {
			throw new ConnectionException(e);
		}
	}
}

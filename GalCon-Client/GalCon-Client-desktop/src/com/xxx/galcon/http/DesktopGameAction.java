/**
 * 
 */
package com.xxx.galcon.http;

import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_GAMES;
import static com.xxx.galcon.http.UrlConstants.GENERATE_GAME;
import static com.xxx.galcon.http.UrlConstants.JOIN_GAME;
import static com.xxx.galcon.http.UrlConstants.PERFORM_MOVES;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.GameLoop;
import com.xxx.galcon.http.request.ClientRequest;
import com.xxx.galcon.http.request.GetClientRequest;
import com.xxx.galcon.http.request.PostClientRequest;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.Move;
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
	public AvailableGames findAvailableGames() throws ConnectionException {
		return (AvailableGames) callURL(new GetClientRequest(), FIND_AVAILABLE_GAMES, new HashMap<String, String>(), new AvailableGames());
	}

	@Override
	public GameBoard performMoves(String gameId, List<Move> moves) throws ConnectionException {
		try {
			JSONObject top = new JSONObject();
			top.put("player", GameLoop.USER);
			top.put("id", gameId);
			JSONArray jsonMoves = new JSONArray();

			for (Move move : moves) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("player", GameLoop.USER);
				jsonObject.put("fromPlanet", move.fromPlanet);
				jsonObject.put("toPlanet", move.toPlanet);
				jsonObject.put("fleet", move.shipsToMove);
				jsonObject.put("duration", 5);

				jsonMoves.put(jsonObject);
			}
			top.put("moves", jsonMoves);
			
			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());
			
			return (GameBoard) callURL(new PostClientRequest(), PERFORM_MOVES, args, new GameBoard());
		} catch (JSONException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public GameBoard generateGame(String player, int width, int height) throws ConnectionException {
		try {
			JSONObject top = new JSONObject();
		
			top.put("player", player);
			top.put("width", width);
			top.put("height", height);
		
			Map<String, String> args = new HashMap<String, String>();

			args.put("json", top.toString());
			return (GameBoard) callURL(new PostClientRequest(), GENERATE_GAME, args, new GameBoard());
			
		} catch (JSONException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public GameBoard joinGame(String id, String player) throws ConnectionException {
		Map<String, String> args = new HashMap<String, String>();
		args.put("player", player);
		args.put("id", id);
		return (GameBoard) callURL(new GetClientRequest(), JOIN_GAME, args, new GameBoard());
	}

	private JsonConvertible callURL(ClientRequest clientRequest, String path, Map<String, String> parameters,
			JsonConvertible converter) throws ConnectionException {
		try {
			String postResponse = executeHttpRequest(clientRequest, path, parameters);
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
	 * This method us used to populate a GameBoard Object with attributes from a
	 * JsonObject.
	 * 
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

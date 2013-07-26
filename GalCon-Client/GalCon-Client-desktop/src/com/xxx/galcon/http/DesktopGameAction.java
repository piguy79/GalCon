/**
 * 
 */
package com.xxx.galcon.http;

import static com.xxx.galcon.http.UrlConstants.ADD_COINS;
import static com.xxx.galcon.http.UrlConstants.FIND_ALL_MAPS;
import static com.xxx.galcon.http.UrlConstants.FIND_CURRENT_GAMES_BY_PLAYER_HANDLE;
import static com.xxx.galcon.http.UrlConstants.FIND_AVAILABLE_GAMES;
import static com.xxx.galcon.http.UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE;
import static com.xxx.galcon.http.UrlConstants.FIND_GAME_BY_ID;
import static com.xxx.galcon.http.UrlConstants.FIND_USER_BY_USER_NAME;
import static com.xxx.galcon.http.UrlConstants.GENERATE_GAME;
import static com.xxx.galcon.http.UrlConstants.JOIN_GAME;
import static com.xxx.galcon.http.UrlConstants.PERFORM_MOVES;
import static com.xxx.galcon.http.UrlConstants.REQUEST_HANDLE_FOR_USER_NAME;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.http.request.ClientRequest;
import com.xxx.galcon.http.request.GetClientRequest;
import com.xxx.galcon.http.request.PostClientRequest;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Player;
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
	public void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String playerHandle) {

		Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);

		callback.onConnectionResult((AvailableGames) callURL(new GetClientRequest(), FIND_AVAILABLE_GAMES, args,
				new AvailableGames()));
	}

	@Override
	public void findAllMaps(UIConnectionResultCallback<Maps> callback) {
		Map<String, String> args = new HashMap<String, String>();
		callback.onConnectionResult((Maps) callURL(new GetClientRequest(), FIND_ALL_MAPS, args, new Maps()));
	}

	@Override
	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves) {
		try {
			JSONObject top = JsonConstructor.performMove(gameId, moves);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((GameBoard) callURL(new PostClientRequest(), PERFORM_MOVES, args,
					new GameBoard()));
		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	public void requestHandleForUserName(UIConnectionResultCallback<HandleResponse> callback, String userName,
			String handle) {
		try {
			JSONObject top = JsonConstructor.requestHandle(userName, handle);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((HandleResponse) callURL(new PostClientRequest(), REQUEST_HANDLE_FOR_USER_NAME,
					args, new HandleResponse()));
		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	public void generateGame(UIConnectionResultCallback<GameBoard> callback, String playerHandle, int width,
			int height, String gameType, Long map, Long rankOfInitialPlayer) {
		try {
			JSONObject top = JsonConstructor.generateGame(playerHandle, width, height, gameType, map,
					rankOfInitialPlayer);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((GameBoard) callURL(new PostClientRequest(), GENERATE_GAME, args,
					new GameBoard()));
		} catch (JSONException e) {
			System.out.println(e);
		}
	}

	@Override
	public void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);
		args.put("id", id);
		callback.onConnectionResult((GameBoard) callURL(new GetClientRequest(), JOIN_GAME, args, new GameBoard()));
	}

	@Override
	public void findGameById(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("id", id);
		args.put("playerHandle", playerHandle);
		callback.onConnectionResult((GameBoard) callURL(new GetClientRequest(), FIND_GAME_BY_ID, args, new GameBoard()));
	}

	@Override
	public void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback, String playerHandle) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);
		callback.onConnectionResult((AvailableGames) callURL(new GetClientRequest(),
				FIND_CURRENT_GAMES_BY_PLAYER_HANDLE, args, new AvailableGames()));
	}

	@Override
	public void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String playerHandle)
			throws ConnectionException {
		Map<String, String> args = new HashMap<String, String>();
		args.put("playerHandle", playerHandle);
		callback.onConnectionResult((AvailableGames) callURL(new GetClientRequest(), FIND_GAMES_WITH_A_PENDING_MOVE,
				args, new AvailableGames()));

	}

	@Override
	public void findUserInformation(UIConnectionResultCallback<Player> callback, String player) {
		Map<String, String> args = new HashMap<String, String>();
		args.put("userName", player);
		callback.onConnectionResult((Player) callURL(new GetClientRequest(), FIND_USER_BY_USER_NAME, args, new Player()));
	}

	private JsonConvertible callURL(ClientRequest clientRequest, String path, Map<String, String> parameters,
			JsonConvertible converter) {
		try {
			String postResponse = executeHttpRequest(clientRequest, path, parameters);
			return buildObjectsFromResponse(converter, postResponse);
		} catch (MalformedURLException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} catch (URISyntaxException e) {
			System.out.println(e);
		}

		return null;
	}

	/**
	 * This method us used to populate a GameBoard Object with attributes from a
	 * JsonObject.
	 * 
	 */
	private JsonConvertible buildObjectsFromResponse(JsonConvertible converter, String postResponse) {
		System.out.println(postResponse);

		try {
			JSONObject gameInformation = new JSONObject(postResponse);
			converter.consume(gameInformation);

			return converter;

		} catch (JSONException e) {
			System.out.println(e);
		}

		return null;
	}

	@Override
	public void addCoins(UIConnectionResultCallback<Player> callback, String playerHandle, Long numCoins) {
		try {
			JSONObject top = JsonConstructor.addCoins(playerHandle, numCoins);

			Map<String, String> args = new HashMap<String, String>();
			args.put("json", top.toString());

			callback.onConnectionResult((Player) callURL(new PostClientRequest(), ADD_COINS, args, new Player()));
		} catch (JSONException e) {
			System.out.println(e);
		}

	}
}

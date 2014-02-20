package com.xxx.galcon;

import static com.xxx.galcon.Config.HOST;
import static com.xxx.galcon.Config.PORT;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.ClientRequest;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.BaseResult;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.GameQueue;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Order;
import com.xxx.galcon.model.People;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.Session;
import com.xxx.galcon.model.base.JsonConvertible;

public class IOSGameAction implements GameAction {

	private GameLoop gameLoop;
	private Config config = new IOSConfig();

	@Override
	public void setGameLoop(GameLoop gameLoop) {
		this.gameLoop = gameLoop;
	}

	@Override
	public String getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSession(String session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exchangeTokenForSession(UIConnectionResultCallback<Session> callback, String authProvider, String token) {
		// TODO Auto-generated method stub

	}

	@Override
	public void matchPlayerToGame(UIConnectionResultCallback<GameBoard> callback, String handle, Long mapToFind) {
		// TODO Auto-generated method stub

	}

	@Override
	public void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void findAllMaps(UIConnectionResultCallback<Maps> callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void findGameById(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void joinGame(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resignGame(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves,
			List<HarvestMove> harvestMoves) {
		// TODO Auto-generated method stub

	}

	@Override
	public void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void findUserInformation(UIConnectionResultCallback<Player> callback, String email) {
		// TODO Auto-generated method stub

	}

	@Override
	public void searchForPlayers(UIConnectionResultCallback<People> callback, String searchTerm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void findConfigByType(UIConnectionResultCallback<Configuration> callback, String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFreeCoins(UIConnectionResultCallback<Player> callback, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCoinsForAnOrder(UIConnectionResultCallback<Player> callback, String handle, List<Order> orders) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteConsumedOrders(UIConnectionResultCallback<Player> callback, String handle, List<Order> orders) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reduceTimeUntilNextGame(UIConnectionResultCallback<Player> callback, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showAd(AdColonyVideoListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadAvailableInventory(UIConnectionResultCallback<Inventory> callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void recoverUsedCoinCount(UIConnectionResultCallback<Player> callback, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void invitePlayerForGame(UIConnectionResultCallback<GameBoard> callback, String requesterHandle,
			String inviteeHandle, Long mapKey) {
		// TODO Auto-generated method stub

	}

	@Override
	public void findFriends(UIConnectionResultCallback<People> callback, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void findPendingIvites(UIConnectionResultCallback<GameQueue> callback, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptInvite(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void declineInvite(UIConnectionResultCallback<BaseResult> callback, String gameId, String handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestHandleForId(UIConnectionResultCallback<HandleResponse> callback, String id, String handle) {
		// TODO Auto-generated method stub

	}

	private JsonConvertible callURL(ClientRequest clientRequest, String path, Map<String, String> parameters,
			JsonConvertible converter) {
		try {
			String postResponse = executeHttpRequest(clientRequest, path, parameters);
			return buildObjectsFromResponse(converter, postResponse);
		} catch (MalformedURLException e) {
			System.out.println(e.getStackTrace());
		} catch (IOException e) {
			System.out.println(e.getStackTrace());
		} catch (URISyntaxException e) {
			System.out.println(e.getStackTrace());
		}

		return null;
	}

	private JsonConvertible buildObjectsFromResponse(JsonConvertible converter, String postResponse) {
		System.out.println(postResponse);

		try {
			JSONObject gameInformation = new JSONObject(postResponse);
			converter.consume(gameInformation);

			return converter;

		} catch (JSONException e) {
			System.out.println(e.getStackTrace());
		}

		return null;
	}

	protected String executeHttpRequest(ClientRequest clientRequest, String path, Map<String, String> parameters)
			throws IOException, URISyntaxException {

		HttpRequestBase request = createTheBaseHttpRequest(clientRequest, path, parameters);

		HttpResponse response = executeResponseOnClient(request);
		HttpEntity responseEntity = response.getEntity();
		if (responseEntity != null) {
			InputStream instream = responseEntity.getContent();

			try {
				return readConnectionData(instream);
			} finally {
				instream.close();
			}
		}

		return "";
	}

	private HttpResponse executeResponseOnClient(HttpRequestBase request) throws IOException, ClientProtocolException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(request);
		return response;
	}

	private HttpRequestBase createTheBaseHttpRequest(ClientRequest clientRequest, String path,
			Map<String, String> parameters) throws URISyntaxException {
		URIBuilder builder = new URIBuilder();

		builder.setScheme("http").setHost(config.getValue(HOST)).setPort(Integer.valueOf(config.getValue(PORT)))
				.setPath(path);

		HttpRequestBase request = clientRequest.createHttpBaseRequest(builder, parameters);
		return request;
	}

	/**
	 * This method is used to read the return value from a HttpUrlConnection.
	 * 
	 * @param connection
	 * @return
	 * @throws IOException
	 */
	private String readConnectionData(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		InputStreamReader input = null;
		try {
			input = new InputStreamReader(is);
			char[] buffer = new char[0x1000];
			int read = 0;
			while ((read = input.read(buffer, 0, buffer.length)) > 0) {
				sb.append(buffer, 0, read);
			}
		} finally {
			if (input != null) {
				input.close();
			}
		}

		return sb.toString();
	}

}

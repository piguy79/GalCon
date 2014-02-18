package com.xxx.galcon.http;

import java.util.List;

import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.BaseResult;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.GameQueue;
import com.xxx.galcon.model.GameQueueItem;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Order;
import com.xxx.galcon.model.People;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.model.Session;

/**
 * This class defines a set of methods used to interact with the server side
 * code.
 * 
 * @author conormullen
 */
public interface GameAction {

	public void setGameLoop(GameLoop gameLoop);

	public String getSession();

	public void setSession(String session);

	public void exchangeTokenForSession(UIConnectionResultCallback<Session> callback, String authProvider, String token);

	public void matchPlayerToGame(UIConnectionResultCallback<GameBoard> callback, String handle, Long mapToFind);

	public void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String handle);

	public void findAllMaps(UIConnectionResultCallback<Maps> callback);

	public void findGameById(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle);

	public void joinGame(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle);

	public void resignGame(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle);

	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves,
			List<HarvestMove> harvestMoves);

	public void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback, String handle);

	public void findUserInformation(UIConnectionResultCallback<Player> callback, String email);

	public void searchForPlayers(UIConnectionResultCallback<People> callback, String searchTerm);

	public void findConfigByType(UIConnectionResultCallback<Configuration> callback, String type);

	public void requestHandleForEmail(UIConnectionResultCallback<HandleResponse> callback, String email, String handle);

	public void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String handle);

	public void addFreeCoins(UIConnectionResultCallback<Player> callback, String handle);

	public void addCoinsForAnOrder(UIConnectionResultCallback<Player> callback, String handle, List<Order> orders);

	public void deleteConsumedOrders(UIConnectionResultCallback<Player> callback, String handle, List<Order> orders);

	public void reduceTimeUntilNextGame(UIConnectionResultCallback<Player> callback, String handle);

	public void showAd(AdColonyVideoListener listener);

	public void loadAvailableInventory(UIConnectionResultCallback<Inventory> callback);

	public void recoverUsedCoinCount(UIConnectionResultCallback<Player> callback, String handle);
	
	public void invitePlayerForGame(UIConnectionResultCallback<GameBoard> callback, String requesterHandle, String inviteeHandle, Long mapKey);
	
	public void findFriends(UIConnectionResultCallback<People> callback, String handle);
	
	public void findPendingIvites(UIConnectionResultCallback<GameQueue> callback, String handle);
	
	public void acceptInvite(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle);
	
	public void declineInvite(UIConnectionResultCallback<BaseResult> callback, String gameId, String handle);


}

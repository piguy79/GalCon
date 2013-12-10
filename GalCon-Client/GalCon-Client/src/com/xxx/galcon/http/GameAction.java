package com.xxx.galcon.http;

import java.util.List;

import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.inappbilling.util.StoreResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Order;
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

	public void matchPlayerToGame(UIConnectionResultCallback<GameBoard> callback, String playerHandle, Long mapToFind);

	public void findAvailableGames(UIConnectionResultCallback<AvailableGames> callback, String playerHandle);

	public void findAllMaps(UIConnectionResultCallback<Maps> callback);

	public void findGameById(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle);

	public void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle);

	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves,
			List<HarvestMove> harvestMoves);

	public void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback, String playerHandle);

	public void findUserInformation(UIConnectionResultCallback<Player> callback, String email);

	public void findConfigByType(UIConnectionResultCallback<Configuration> callback, String type);

	public void requestHandleForEmail(UIConnectionResultCallback<HandleResponse> callback, String email, String handle);

	public void findGamesWithPendingMove(UIConnectionResultCallback<AvailableGames> callback, String playerHandle)
			throws ConnectionException;

	public void addCoins(UIConnectionResultCallback<Player> callback, String playerHandle, int numCoins)
			throws ConnectionException;

	public void addCoinsForAnOrder(UIConnectionResultCallback<Player> callback, String playerHandle, List<Order> orders)
			throws ConnectionException;

	public void deleteConsumedOrders(UIConnectionResultCallback<Player> callback, String playerHandle,
			List<Order> orders);

	public void reduceTimeUntilNextGame(UIConnectionResultCallback<Player> callback, String playerHandle,
			Long timeRemaining, Long usedCoins) throws ConnectionException;

	public void showAd(AdColonyVideoListener listner);

	public void purchaseCoins(InventoryItem inventoryItem, UIConnectionResultCallback<Player> callback);

	public void loadStoreInventory(Inventory inventory, StoreResultCallback<Inventory> callback);

	public void loadAvailableInventory(UIConnectionResultCallback<Inventory> callback);

	public void consumeOrders(List<Order> orders);

	public void consumeExistingOrders();

	public void recoverUsedCoinCount(UIConnectionResultCallback<Player> callback, String playerHandle)
			throws ConnectionException;

}

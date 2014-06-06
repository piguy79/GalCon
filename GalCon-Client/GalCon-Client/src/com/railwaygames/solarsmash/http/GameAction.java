package com.railwaygames.solarsmash.http;

import java.util.List;

import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.config.Configuration;
import com.railwaygames.solarsmash.model.AvailableGames;
import com.railwaygames.solarsmash.model.BaseResult;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.model.GameCount;
import com.railwaygames.solarsmash.model.GameQueue;
import com.railwaygames.solarsmash.model.HandleResponse;
import com.railwaygames.solarsmash.model.HarvestMove;
import com.railwaygames.solarsmash.model.Inventory;
import com.railwaygames.solarsmash.model.Leaderboards;
import com.railwaygames.solarsmash.model.Maps;
import com.railwaygames.solarsmash.model.Move;
import com.railwaygames.solarsmash.model.Order;
import com.railwaygames.solarsmash.model.People;
import com.railwaygames.solarsmash.model.Player;
import com.railwaygames.solarsmash.model.Session;

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

	public void findAllMaps(UIConnectionResultCallback<Maps> callback);

	public void findGameById(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle);

	public void joinGame(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle);

	public void resignGame(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle);

	public void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves,
			List<HarvestMove> harvestMoves);

	public void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback, String handle);

	public void findUserInformation(UIConnectionResultCallback<Player> callback, String id, String authProvider);

	public void searchForPlayers(UIConnectionResultCallback<People> callback, String searchTerm);

	public void findConfigByType(UIConnectionResultCallback<Configuration> callback, String type);

	public void requestHandleForId(UIConnectionResultCallback<HandleResponse> callback, String id, String handle,
			String authProvider);

	public void findGamesWithPendingMove(UIConnectionResultCallback<GameCount> callback, String handle);

	public void addFreeCoins(UIConnectionResultCallback<Player> callback, String handle);

	public void addCoinsForAnOrder(UIConnectionResultCallback<Player> callback, String handle, List<Order> orders);

	public void deleteConsumedOrders(UIConnectionResultCallback<Player> callback, String handle, List<Order> orders);

	public void showAd();

	public void loadAvailableInventory(UIConnectionResultCallback<Inventory> callback);

	public void invitePlayerForGame(UIConnectionResultCallback<GameBoard> callback, String requesterHandle,
			String inviteeHandle, Long mapKey);

	public void findFriends(UIConnectionResultCallback<People> callback, String handle);

	public void findPendingIvites(UIConnectionResultCallback<GameQueue> callback, String handle);

	public void acceptInvite(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle);

	public void declineInvite(UIConnectionResultCallback<BaseResult> callback, String gameId, String handle);

	public void findMatchingFriends(UIConnectionResultCallback<People> callback, List<String> authIds, String handle,
			String authProvider);

	public void addProviderToUser(UIConnectionResultCallback<Player> callback, String handle, String id,
			String authProvider);

	public void cancelGame(UIConnectionResultCallback<BaseResult> callback, String handle, String gameId);

	public void claimVictory(UIConnectionResultCallback<GameBoard> callback, String handle, String gameId);
	
	public void findLeaderboardById(UIConnectionResultCallback<Leaderboards> callback, String id);

}

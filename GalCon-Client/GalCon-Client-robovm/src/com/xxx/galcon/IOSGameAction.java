package com.xxx.galcon;

import java.util.List;

import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.config.Configuration;
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

public class IOSGameAction implements GameAction {

	@Override
	public void setGameLoop(GameLoop gameLoop) {
		// TODO Auto-generated method stub

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

}

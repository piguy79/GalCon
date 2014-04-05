package com.xxx.galcon;

import java.util.List;

import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.AvailableGames;
import com.xxx.galcon.model.BaseResult;
import com.xxx.galcon.model.GameBoard;
import com.xxx.galcon.model.GameQueue;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Maps;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.People;
import com.xxx.galcon.model.Player;

public class UIConnectionWrapper {
	private static GameAction gameAction;

	private UIConnectionWrapper() {
		super();
	}

	public static void setGameAction(GameAction gameAction) {
		UIConnectionWrapper.gameAction = gameAction;
	}

	public static void performMoves(UIConnectionResultCallback<GameBoard> callback, String gameId, List<Move> moves,
			List<HarvestMove> harvestMoves) {
		gameAction.performMoves(callback, gameId, moves, harvestMoves);
	}

	public static void findAllMaps(UIConnectionResultCallback<Maps> callback) {
		gameAction.findAllMaps(callback);
	}

	public static void findGameById(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle) {
		gameAction.findGameById(callback, id, playerHandle);
	}

	public static void resignGame(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle) {
		gameAction.resignGame(callback, gameId, handle);
	}

	public static void findCurrentGamesByPlayerHandle(UIConnectionResultCallback<AvailableGames> callback,
			String playerHandle) {
		gameAction.findCurrentGamesByPlayerHandle(callback, playerHandle);
	}

	public static void joinGame(UIConnectionResultCallback<GameBoard> callback, String id, String playerHandle) {
		gameAction.joinGame(callback, id, playerHandle);
	}

	public static void reduceTimeUntilCoins(UIConnectionResultCallback<Player> callback, String playerHandle) {
		gameAction.reduceTimeUntilNextGame(callback, playerHandle);
	}

	public static void findconfigByType(UIConnectionResultCallback<Configuration> callback, String type) {
		gameAction.findConfigByType(callback, type);
	}

	public static void searchForPlayers(UIConnectionResultCallback<People> callback, String searchTerm) {
		gameAction.searchForPlayers(callback, searchTerm);
	}
	
	public static void invitePlayerForGame(UIConnectionResultCallback<GameBoard> callback, String requesterHandle, String inviteeHandle, Long mapKey){
		gameAction.invitePlayerForGame(callback, requesterHandle, inviteeHandle, mapKey);
	}
	
	public static void findFriends(UIConnectionResultCallback<People> callback, String handle){
		gameAction.findFriends(callback, handle);
	}
	
	public static void findPendingInvites(UIConnectionResultCallback<GameQueue> callback, String handle){
		gameAction.findPendingIvites(callback, handle);
	}
	
	public static void acceptInvite(UIConnectionResultCallback<GameBoard> callback, String gameId, String handle){
		gameAction.acceptInvite(callback, gameId, handle);
	}
	
	public static void declineInvite(UIConnectionResultCallback<BaseResult> callback, String gameId, String handle){
		gameAction.declineInvite(callback, gameId, handle);
	}

	public static void recoverUsedCoinCount(UIConnectionResultCallback<Player> callback, String handle) {
		gameAction.recoverUsedCoinCount(callback, handle);
	}
	
	public static void cancelGame(UIConnectionResultCallback<BaseResult> callback, String handle, String gameId){
		gameAction.cancelGame(callback, handle, gameId);
	}
}

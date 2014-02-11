/**
 * 
 */
package com.xxx.galcon.http;

/**
 * @author conormullen
 * 
 */
public class UrlConstants {

	public static final String FIND_AVAILABLE_GAMES = "/findAvailableGames";

	public static final String FIND_ALL_MAPS = "/findAllMaps";

	public static final String GENERATE_GAME = "/generateGame";

	public static final String PERFORM_MOVES = "/performMoves";

	public static final String ADD_FREE_COINS = "/addFreeCoins";

	public static final String ADD_COINS_FOR_AN_ORDER = "/addCoinsForAnOrder";

	public static final String DELETE_CONSUMED_ORDERS = "/deleteConsumedOrders";

	public static final String REDUCE_TIME = "/reduceTimeUntilNextGame";

	public static final String JOIN_GAME = "/joinGame";

	public static final String FIND_GAME_BY_ID = "/findGameById";

	public static final String FIND_CURRENT_GAMES_BY_PLAYER_HANDLE = "/findCurrentGamesByPlayerHandle";

	public static final String FIND_GAMES_WITH_A_PENDING_MOVE = "/findGamesWithPendingMove";
	
	public static final String FIND_USER_BY_EMAIL = "/findUserByEmail";
	
	public static final String SEARCH_FOR_USERS = "/search/user";

	public static final String FIND_AVAILABLE_INVENTORY = "/inventory";

	public static final String FIND_CONFIG_BY_TYPE = "/config";

	public static final String REQUEST_HANDLE_FOR_EMAIL = "/requestHandleForEmail";

	public static final String FIND_RANK_INFORMATION = "/rank";

	public static final String MATCH_PLAYER_TO_GAME = "/matchPlayertoGame";
	
	public static final String RESIGN_GAME = "/games/:gameId/resign";
	
	public static final String FIND_FRIENDS = "/friends";

	public static final String RECOVER_USED_COINS_COUNT = "/updateUsedCoinsRecover";
	
	public static final String INVITE_USER_TO_PLAY = "/gamequeue/invite";

	public static final String EXCHANGE_TOKEN_FOR_SESSION = "/sessions/exchangeToken";

	public static final String GAMES_FOR_A_USER = "/gamesForAUser";

}

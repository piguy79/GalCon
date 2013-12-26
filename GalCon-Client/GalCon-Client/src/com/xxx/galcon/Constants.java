package com.xxx.galcon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
	public static final String APP_TITLE = "GalCon";

	public static class UI {
		public static final String DEFAULT_BG_COLOR = "defaultBgColor";

		public static final String SMALL_FONT = "smallFont";
		public static final String DEFAULT_FONT = "defaultFont";
		public static final String DEFAULT_FONT_BLACK = "defaultFontBlack";
		public static final String LARGE_FONT = "largeFont";

		public static final String TEXT_FIELD = "textField";

		public static final String OK_BUTTON = "okButton";
		public static final String WAIT_BUTTON = "waitButton";
		public static final String GREEN_BUTTON = "greenButton";
		public static final String GREEN_BUTTON_TEXT = "greenButtonText";
		public static final String GRAY_BUTTON = "grayButton";
		public static final String GRAY_BUTTON_TEXT = "grayButtonText";
		public static final String CELL_BG = "cellBg";
		public static final String DIALOG_BG = "dialogBg";
		public static final String GOOGLE_PLUS_SIGN_IN_BUTTON = "googlePlusSignInButton";
	}

	public static class Auth {
		public static final String SOCIAL_AUTH_PROVIDER = "socialAuthProvider";
		public static final String SOCIAL_AUTH_PROVIDER_GOOGLE = "google";
		public static final String LAST_SESSION_ID = "lastSessionId";
		public static final String EMAIL = "email";
	}

	public static final String CONNECTION_ERROR_MESSAGE = "Unable to connect. Please try again.";
	public static final String GALCON_PREFS = "galConPrefs";
	public static final String OWNER_NO_ONE = "NO_ONE";
	public static final String PLANETS = "planets";
	public static final String CURRENT_ROUND = "currentRound";
	public static final String ROUND_NUMBER = "roundNumber";
	public static final String CREATED_DATE = "createdDate";
	public static final String WINNING_DATE = "winningDate";
	public static final String PLAYER = "player";
	public static final String PLAYER_HANDLE = "playerHandle";
	public static final String PLAYERS = "players";
	public static final String ITEMS = "items";
	public static final String ID = "_id";
	public static final String EMAIL = "email";
	public static final String NAME = "name";
	public static final String CREATED = "created";
	public static final String REASON = "reason";
	public static final String HANDLE = "handle";
	public static final String SHIP_REGEN_RATE = "shipRegenRate";
	public static final float SHIP_REGEN_RATE_MAX = 5.0f;
	public static final String NUMBER_OF_SHIPS = "numberOfShips";
	public static final String ABILITY = "ability";
	public static final String OWNER_HANDLE = "ownerHandle";
	public static final String POSITION = "position";
	public static final String X = "x";
	public static final String Y = "y";
	public static final String WIDTH = "width";
	public static final String END_GAME_INFO = "endGameInformation";
	public static final String DRAW = "draw";
	public static final String LOSER_HANDLES = "loserHandles";
	public static final String HEIGHT = "height";
	public static final String JOIN = "Join";
	public static final String LEADERBOARDS = "Leaderboards";
	public static final String RANK = "rank";
	public static final String LEVEL = "level";
	public static final String XP = "xp";
	public static final String CURRENT_GAMES = "currentGames";
	public static final String START_FROM = "startFrom";
	public static final String END_AT = "endAt";
	public static final String RANK_INFO = "rankInfo";
	public static final String WINNER_HANDLE = "winnerHandle";
	public static final String ABILITY_SPEED = "speedModifier";
	public static final String ABILITY_ATTACK_INCREASE = "attackModifier";
	public static final String ABILITY_DEFENCE_INCREASE = "defenseModifier";
	public static final String ABILITY_REGEN_BLOCK = "blockModifier";
	public static final String XP_AWARD_TO_WINNER = "xpAwardToWinner";
	public static final String PLAYERS_WHO_MOVED = "playersWhoMoved";
	public static final String RANK_OF_INITIAL_PLAYER = "rankOfInitialPlayer";
	public static final String MAP = "map";
	public static final String CREATED_TIME = "createdTime";
	public static final String KEY = "key";
	public static final String AVAILABLE_FROM_LEVEL = "availableFromLevel";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String VERSION = "version";
	public static final String TYPE = "type";
	public static final String VALUES = "values";
	public static final String WATCHED_AD = "watchedAd";
	public static final String COINS = "coins";
	public static final String USED_COINS = "usedCoins";

	public static SimpleDateFormat timeRemainingFormat = new SimpleDateFormat("mm:ss");

	public static final Map<String, String> PLANET_ABILITIES = new HashMap<String, String>() {
		{
			put(ABILITY_SPEED, "ship movement speed");
			put(ABILITY_DEFENCE_INCREASE, "planet defence");
			put(ABILITY_ATTACK_INCREASE, "attack power");
			put(ABILITY_REGEN_BLOCK, "block opponents regen");
		}
	};

	public static final List<String> gameTypes = new ArrayList<String>() {
		{
			add("attackIncrease");
			add("defenceIncrease");
			add("speedIncrease");
			add("regenBlock");
		}
	};

}

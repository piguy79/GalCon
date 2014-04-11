package com.railwaygames.solarsmash;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;

public class Constants {
	public static final String APP_TITLE = "Solar Smash";
	
	public static class Config{
		public static final String XP_AWARDED_TO_WINNER = "xpForWinning";
		public static final String MAX_NUM_OF_OPEN_GAMES = "maxNumberOfOpenGames";

	}

	public static class UI {
		public static final String DEFAULT_BG_COLOR = "defaultBgColor";

		public static final String X_SMALL_FONT = "xSmallFont";
		public static final String X_SMALL_FONT_GREEN = "xSmallFontGreen";
		public static final String X_SMALL_FONT_RED = "xSmallFontRed";

		public static final String SMALL_FONT = "smallFont";

		public static final String DEFAULT_FONT = "defaultFont";
		public static final String DEFAULT_FONT_BLACK = "defaultFontBlack";
		public static final String DEFAULT_FONT_GREEN = "defaultFontGreen";
		public static final String DEFAULT_FONT_RED = "defaultFontRed";
		public static final String DEFAULT_FONT_YELLOW = "defaultFontYellow";
		public static final String LARGE_FONT = "largeFont";
		public static final String LARGE_FONT_GREEN = "largeFontGreen";
		public static final String LARGE_FONT_RED = "largeFontRed";
		public static final String X_LARGE_FONT = "xLargeFont";
		public static final String LARGE_FONT_BLACK = "largeFontBlack";

		public static final String TEXT_FIELD = "textField";

		public static final String BASIC_BUTTON = "basicButton";
		public static final String BASIC_BUTTON_TEXT = "basicButtonText";

		public static final String OK_BUTTON = "okButton";
		public static final String QUESTION_MARK = "questionMark";
		public static final String WAIT_BUTTON = "waitButton";
		public static final String GREEN_BUTTON = "greenButton";
		public static final String GREEN_BUTTON_TEXT = "greenButtonText";
		public static final String GREEN_BUTTON_TEXT_SMALL = "greenButtonTextSmall";
		public static final String GRAY_BUTTON = "grayButton";
		public static final String GRAY_BUTTON_TEXT = "grayButtonText";
		public static final String CELL_BG = "cellBg";
		public static final String COIN = "coin";
		public static final String DIALOG_BG = "dialogBg";
		public static final String GOOGLE_PLUS_SIGN_IN_BUTTON = "googlePlusSignInButton";
		public static final String FACEBOOK_SIGN_IN_BUTTON = "facebookSignInButton";

		public static final String EXPLOSION_PARTICLE = "explosionParticle";

		public static final String GOOGLE_PLUS_SIGN_IN_NORMAL = "googlePlusSignInImageButton";
		public static final String GALCON_SEARCH_IMAGE = "galconSearchImage";
		public static final String COUNT_LABEL = "countLabel";
		public static final String SCROLL_HIGHLIGHT = "scrollHightlight";

		public static final String SHARE_ICON = "shareIcon";
		public static final String PLAY_ARROW = "playArrow";

		public static final String HIGHLIGHT_BAR = "highlightBar";
		public static final String BACK_ARROW_WHITE = "backArrowWhite";

		public static final String XP_BAR_COVER = "xpBarCover";
		public static final String XP_BAR_MAIN = "xpBarMain";
		public static final String XP_BAR_ARROW = "xpBarArrow";

	}

	public static class Colors {
		public static Color USER_SHIP_FILL = Color.GREEN;
		public static Color ENEMY_SHIP_FILL = new Color(1.0f, 0.2f, 0.2f, 1.0f);
		public static Color NEUTRAL  = Color.valueOf("999B9C");
	}

	public static class Auth {
		public static final String SOCIAL_AUTH_PROVIDER = "socialAuthProvider";
		public static final String SOCIAL_AUTH_PROVIDER_GOOGLE = "google";
		public static final String SOCIAL_AUTH_PROVIDER_FACEBOOK = "facebook";
		public static final String LAST_SESSION_ID = "lastSessionId";
		public static final String EMAIL = "email";
	}

	public static final String CONNECTION_ERROR_MESSAGE = "Unable to connect.\n\nTouch the screen\nto try again.";
	public static final String GALCON_PREFS = "galConPrefs";
	public static final String OWNER_NO_ONE = "NO_ONE";
	public static final String PLANETS = "planets";
	public static final String CURRENT_ROUND = "round";
	public static final String ROUND_NUMBER = "num";
	public static final String CREATED_DATE = "createdDate";
	public static final String DATE = "date";
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
	public static final String SHIP_REGEN_RATE = "regen";
	public static final float SHIP_REGEN_RATE_MAX = 5.0f;
	public static final String NUMBER_OF_SHIPS = "ships";
	public static final String ABILITY = "ability";
	public static final String OWNER_HANDLE = "handle";
	public static final String POSITION = "pos";
	public static final String X = "x";
	public static final String Y = "y";
	public static final String WIDTH = "width";
	public static final String END_GAME_INFO = "endGame";
	public static final String DRAW = "draw";
	public static final String LOSER_HANDLES = "loserHandles";
	public static final String HEIGHT = "height";
	public static final String JOIN = "Join";
	public static final String LEADERBOARDS = "Leaderboards";
	public static final String RANK = "rank";
	public static final String RANKS = "ranks";
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
	public static final String PLAYERS_WHO_MOVED = "moved";
	public static final String RANK_OF_INITIAL_PLAYER = "rankOfInitialPlayer";
	public static final String MAP = "map";
	public static final String CREATED_TIME = "createdTime";
	public static final String KEY = "key";
	public static final String AVAILABLE_FROM_XP = "availableFromXp";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String VERSION = "version";
	public static final String TYPE = "type";
	public static final String VALUES = "values";
	public static final String WATCHED_AD = "watchedAd";
	public static final String COINS = "coins";
	public static final String CANCELED = "canceled";
	public static final String USED_COINS = "usedCoins";
	public static final String POPULATION = "population";
	public static final String SOCIAL = "social";
	public static final String XP_FROM_PLANET_CAPTURE = "xpForPlanetCapture";
	public static final String CONFIG = "config";


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

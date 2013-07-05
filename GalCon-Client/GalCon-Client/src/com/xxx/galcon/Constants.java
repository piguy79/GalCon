package com.xxx.galcon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
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
	public static final String CURRENT = "Current";
	public static final String CREATE = "Create";
	public static final String RANK = "rank";
	public static final String LEVEL = "level";
	public static final String XP = "xp";
	public static final String CURRENT_GAMES = "currentGames";
	public static final String START_FROM = "startFrom";
	public static final String END_AT = "endAt";
	public static final String RANK_INFO = "rankInfo";
	public static final String WINNER_HANDLE = "winnerHandle";
	public static final String ABILITY_SPEED = "SPEED";
	public static final String ABILITY_ATTACK_INCREASE = "ATTACK_INC";
	public static final String ABILITY_DEFENCE_INCREASE = "DEF_INC";
	public static final String ABILITY_REGEN_BLOCK = "REGEN_BLOCK";
	public static final String XP_AWARD_TO_WINNER ="xpAwardToWinner";
	public static final String PLAYERS_WHO_MOVED = "playersWhoMoved";

	

	public static final Map<String, String> PLANET_ABILITIES = new HashMap<String, String>() {
		{
			put(ABILITY_SPEED, "Increased ship movement speed");
			put(ABILITY_DEFENCE_INCREASE, "Increased planet defence");
			put(ABILITY_ATTACK_INCREASE, "Increased attack power");
			put(ABILITY_REGEN_BLOCK, "The ability to block opponents Regen");
		}
	};
	
	public static final List<String> gameTypes = new ArrayList<String>(){
		{
			add("attackIncrease");
			add("defenceIncrease");
			add("speedIncrease");
			add("regenBlock");
		}
	};

}

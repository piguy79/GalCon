package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.model.base.JsonConvertible;
import com.railwaygames.solarsmash.screen.BoardScreen;

public class GameBoard extends JsonConvertible {
	public String id;
	public DateTime createdDate;
	public List<Player> players;
	public int widthInTiles = 0;
	public int heightInTiles = 0;
	public Social social;
	public Long map;
	public Long rankOfInitialPlayer;
	public List<Planet> planets = new ArrayList<Planet>();
	public RoundInformation roundInformation = new RoundInformation();
	public EndGameInformation endGameInformation = new EndGameInformation();
	public List<Move> movesInProgress = new ArrayList<Move>();
	public GameConfig gameConfig = new GameConfig();
	public Long moveTime;
	public boolean ai;
	public java.util.Map<String, Integer> handleToVictoriesVsOpponent = new HashMap<String, Integer>();
	public java.util.Map<String, Record> handleToVictoriesInLast10 = new HashMap<String, Record>();
	public java.util.Map<String, Record> handleToOverallRecord = new HashMap<String, Record>();

	public GameBoard() {

	}

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		this.players = new ArrayList<Player>();
		JSONArray playersJson = jsonObject.getJSONArray(Constants.PLAYERS);
		for (int i = 0; i < playersJson.length(); i++) {
			Player player = new Player();
			player.consume(playersJson.getJSONObject(i));
			this.players.add(player);
		}
		this.planets = new ArrayList<Planet>();
		JSONArray planetsJson = jsonObject.getJSONArray(Constants.PLANETS);
		for (int i = 0; i < planetsJson.length(); i++) {
			JSONObject jsonPlanet = planetsJson.getJSONObject(i);
			Planet planet = new Planet();
			planet.consume(jsonPlanet);
			this.planets.add(planet);
		}
		this.id = jsonObject.getString(Constants.ID);
		this.widthInTiles = jsonObject.getInt(Constants.WIDTH);
		this.ai = jsonObject.optBoolean(Constants.AI);

		if (jsonObject.has("social")) {
			this.social = new Social();
			this.social.consume(jsonObject.getJSONObject(Constants.SOCIAL));
		}
		this.heightInTiles = jsonObject.getInt(Constants.HEIGHT);
		this.rankOfInitialPlayer = jsonObject.getLong(Constants.RANK_OF_INITIAL_PLAYER);
		this.map = jsonObject.getLong(Constants.MAP);
		this.createdDate = formatDate(jsonObject, Constants.CREATED_DATE);

		JSONObject endGame = jsonObject.getJSONObject(Constants.END_GAME_INFO);

		this.endGameInformation.consume(endGame);

		this.roundInformation.consume(jsonObject.getJSONObject(Constants.CURRENT_ROUND));
		this.moveTime = jsonObject.optLong("moveTime");

		JSONArray moves = jsonObject.optJSONArray("moves");
		if (moves != null) {
			for (int i = 0; i < moves.length(); ++i) {
				JSONObject jsonMove = moves.getJSONObject(i);

				Move move = new Move();
				move.consume(jsonMove);
				movesInProgress.add(move);
			}
		}
		JSONObject stats = jsonObject.optJSONObject("stats");
		if (stats != null) {
			JSONObject jsonVictoryMap = stats.optJSONObject("victoryMap");
			if (jsonVictoryMap != null) {
				for (Iterator<?> iter = jsonVictoryMap.keys(); iter.hasNext();) {
					String key = (String) iter.next();
					handleToVictoriesVsOpponent.put(key, jsonVictoryMap.getInt(key));
				}
			}

			for (Iterator<?> iter = stats.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				if (!key.equals("victoryMap")) {
					JSONObject jsonPlayerStats = stats.getJSONObject(key);
					JSONObject jsonRecord = jsonPlayerStats.optJSONObject("last10");
					if (jsonRecord != null) {
						handleToVictoriesInLast10.put(key,
								new Record(jsonRecord.getInt("wins"), jsonRecord.getInt("losses")));
					}

					jsonRecord = jsonPlayerStats.optJSONObject("overall");
					if (jsonRecord != null) {
						handleToOverallRecord.put(key,
								new Record(jsonRecord.getInt("wins"), jsonRecord.getInt("losses")));
					}
				}
			}
		}

		gameConfig.consume(jsonObject.optJSONObject("config"));
	}

	public List<Player> allPlayersExcept(String playerHandleToExclude) {
		List<Player> otherPlayers = new ArrayList<Player>();

		for (Player player : players) {
			if (!player.handle.equals(playerHandleToExclude)) {
				otherPlayers.add(player);
			}
		}

		return otherPlayers;
	}

	private List<String> ownedPlanetAbilities = new ArrayList<String>();

	public List<String> ownedPlanetAbilities(Player player) {
		ownedPlanetAbilities.clear();

		for (int i = 0; i < planets.size(); ++i) {
			Planet planet = planets.get(i);
			if (planet.isOwnedBy(player.handle) && planet.hasAbility() && planet.isAlive()) {
				ownedPlanetAbilities.add(planet.ability);
			}
		}

		return ownedPlanetAbilities;
	}

	public int ownedPlanetRegen(Player player) {
		int regen = 0;
		for (int i = 0; i < planets.size(); ++i) {
			Planet planet = planets.get(i);
			if (planet.isOwnedBy(player.handle)
					|| (planet.isHome && planet.isOwnedBy(Constants.OWNER_NO_ONE) && !player.handle.equals(GameLoop
							.getUser().handle))) {
				regen += planet.regen;
			}
		}

		return regen;
	}

	private List<String> ownedPlanetsUnderHarvest = new ArrayList<String>();

	public List<String> ownedPlanetsUnderHarvest(Player player) {
		ownedPlanetsUnderHarvest.clear();

		for (int i = 0; i < planets.size(); ++i) {
			Planet planet = planets.get(i);
			if (planet.isOwnedBy(player.handle) && planet.hasAbility() && planet.isUnderHarvest()) {
				ownedPlanetsUnderHarvest.add(planet.ability);
			}
		}

		return ownedPlanetsUnderHarvest;
	}

	public Planet getPlanet(String name) {
		for (int i = 0; i < planets.size(); ++i) {
			Planet planet = planets.get(i);
			if (planet.name.equals(name)) {
				return planet;
			}
		}

		return null;
	}

	public boolean hasWinner() {
		return endGameInformation.winnerHandle != null && !endGameInformation.winnerHandle.isEmpty()
				&& !endGameInformation.winnerHandle.equals("GAME_DECLINE");
	}

	public boolean wasADraw() {
		return endGameInformation.draw;
	}

	public Move selectedMove() {
		for (Move move : movesInProgress) {
			if (move.selected != -1f) {
				return move;
			}
		}
		return null;
	}

	public Player getEnemy() {
		if (players.size() < 2) {
			Player waitingForOpponent = new Player();
			waitingForOpponent.xp = -1;
			waitingForOpponent.handle = BoardScreen.Labels.waitingLabel(social);
			return waitingForOpponent;
		}

		if (players.get(0).handle.equals(GameLoop.getUser().handle)) {
			return players.get(1);
		}

		return players.get(0);
	}

	public Player getUser() {
		if (players.get(0).handle.equals(GameLoop.getUser().handle)) {
			return players.get(0);
		}

		return players.get(1);
	}

	public boolean isClaimAvailable() {
		return roundInformation.players.size() == 1 && roundInformation.players.contains(GameLoop.getUser().handle)
				&& moveTimeIsPastTimeout() && !hasWinner() && !wasADraw() && players.size() > 1;
	}

	private boolean moveTimeIsPastTimeout() {
		Long currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
		return (currentTime - moveTime) >= Long.parseLong(gameConfig.getValue("claimTimeout"));
	}

	public static class Record {
		public int wins;
		public int losses;

		public Record(int wins, int losses) {
			this.wins = wins;
			this.losses = losses;
		}
	}
}

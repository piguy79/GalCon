package com.railwaygames.solarsmash.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class GameBoard extends JsonConvertible {
	public String id;
	public Date createdDate;
	public List<Player> players;
	public int widthInTiles = 0;
	public int heightInTiles = 0;
	public Long createdTime;
	public Social social;
	public Long map;
	public Long rankOfInitialPlayer;
	public List<Planet> planets = new ArrayList<Planet>();
	public RoundInformation roundInformation = new RoundInformation();
	public EndGameInformation endGameInformation = new EndGameInformation();
	public List<Move> movesInProgress = new ArrayList<Move>();
	public GameConfig gameConfig = new GameConfig();

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

		if (jsonObject.has("social")) {
			this.social = new Social();
			this.social.consume(jsonObject.getJSONObject(Constants.SOCIAL));
		}
		this.heightInTiles = jsonObject.getInt(Constants.HEIGHT);
		this.rankOfInitialPlayer = jsonObject.getLong(Constants.RANK_OF_INITIAL_PLAYER);
		this.map = jsonObject.getLong(Constants.MAP);
		this.createdTime = jsonObject.getLong(Constants.CREATED_TIME);

		JSONObject endGame = jsonObject.getJSONObject(Constants.END_GAME_INFO);

		this.endGameInformation.consume(endGame);
		this.createdDate = formatDate(jsonObject, Constants.CREATED_DATE);

		this.roundInformation.consume(jsonObject.getJSONObject(Constants.CURRENT_ROUND));

		JSONArray moves = jsonObject.optJSONArray("moves");
		if (moves != null) {
			for (int i = 0; i < moves.length(); ++i) {
				JSONObject jsonMove = moves.getJSONObject(i);

				Move move = new Move();
				move.consume(jsonMove);
				movesInProgress.add(move);
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
		return endGameInformation.winnerHandle != null && !endGameInformation.winnerHandle.isEmpty();
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
}

package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.math.Vector2;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.base.JsonConvertible;

public class GameBoard extends JsonConvertible {
	public String id;
	public Date createdDate;
	public List<Player> players;
	public int widthInTiles = 0;
	public int heightInTiles = 0;
	public List<Planet> planets = new ArrayList<Planet>();
	public RoundInformation roundInformation = new RoundInformation();
	public EndGameInformation endGameInformation = new EndGameInformation();
	public List<Move> movesInProgress = new ArrayList<Move>();

	public GameBoard() {

	}

	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
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
		this.heightInTiles = jsonObject.getInt(Constants.HEIGHT);

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
			if (planet.isOwnedBy(player) && planet.hasAbility()) {
				ownedPlanetAbilities.add(planet.ability);
			}
		}

		return ownedPlanetAbilities;
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
		for(Move move : movesInProgress){
			if(move.selected != -1f){
				return move;
			}
		}
		return null;
	}
}

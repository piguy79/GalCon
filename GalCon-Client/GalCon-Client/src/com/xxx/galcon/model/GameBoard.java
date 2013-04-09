package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xxx.galcon.Constants;
import com.xxx.galcon.model.base.JsonConvertible;

public class GameBoard implements JsonConvertible {
	public String id;
	public Date createdDate;
	public List<String> players;
	public int widthInTiles = 0;
	public int heightInTiles = 0;
	public List<Planet> planets = new ArrayList<Planet>();
	public int roundNumber;
	public String currentPlayerToMove;
	public List<Move> movesInProgress = new ArrayList<Move>();

	public GameBoard() {

	}

	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		this.players = new ArrayList<String>();
		JSONArray playersJson = jsonObject.getJSONArray(Constants.PLAYERS);
		for (int i = 0; i < playersJson.length(); i++) {
			String player = playersJson.getString(i);
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

		JSONObject roundInfo = jsonObject.getJSONObject(Constants.CURRENT_ROUND);
		roundNumber = roundInfo.getInt(Constants.ROUND_NUMBER);
		currentPlayerToMove = roundInfo.getString(Constants.PLAYER);

		JSONArray moves = jsonObject.optJSONArray("moves");
		if (moves != null) {
			for (int i = 0; i < moves.length(); ++i) {
				JSONObject jsonMove = moves.getJSONObject(i);

				Move move = new Move();
				move.fromPlanet = jsonMove.getString("fromPlanet");
				move.toPlanet = jsonMove.getString("toPlanet");
				move.shipsToMove = jsonMove.getInt("fleet");
				move.duration = jsonMove.getInt("duration");

				movesInProgress.add(move);
			}
		}
	}
}

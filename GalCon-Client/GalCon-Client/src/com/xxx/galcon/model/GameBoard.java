package com.xxx.galcon.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	public String winner = "";
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
		this.winner = jsonObject.optString(Constants.WINNER);
		
		assignCreatedDate(jsonObject);


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
				move.player = jsonMove.getString("player");

				movesInProgress.add(move);
			}
		}
	}

	private void assignCreatedDate(JSONObject jsonObject) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm'Z'");
		try {
			this.createdDate = format.parse(jsonObject.getString(Constants.CREATED_DATE));
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public List<String> allPlayersExcept(String playerToExclude){
		List<String> otherPlayers = new ArrayList<String>();
		
		for(String player : players){
			if(!player.equals(playerToExclude)){
				otherPlayers.add(player);
			}
		}
		
		return otherPlayers;
	}
}

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
	public int numberOfTilesWide = 0;
	public int numberOfTilesTall = 0;
	public List<Planet> planets = new ArrayList<Planet>();

	public GameBoard() {

	}

	@Override
	public void consume(JSONObject jsonObject) {
		try {
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

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

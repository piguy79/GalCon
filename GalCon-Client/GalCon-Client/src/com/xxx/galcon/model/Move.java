package com.xxx.galcon.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import aurelienribon.tweenengine.Tween;

import com.badlogic.gdx.math.Vector2;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.base.JsonConstructable;
import com.xxx.galcon.model.base.JsonConvertible;
import com.xxx.galcon.model.tween.MoveTween;

public class Move extends JsonConvertible implements JsonConstructable {

	public String fromPlanet;
	public String toPlanet;
	public int shipsToMove = 0;
	public float duration = 0;
	public String playerHandle;
	public Point previousPosition = new Point();
	public Point currentPosition = new Point();
	public Point startPosition = new Point();
	public Point endPosition = new Point();
	public int startingRound;
	public boolean executed;
	public BattleStats battleStats;
	
	public Tween animation;

	public Point currentAnimation = new Point();;

	public float selected = -1f;

	public Move() {
		super();
		this.animation = Tween.to(this, MoveTween.POSITION_XY, 1.2f);

	}

	public boolean belongsToPlayer(Player player) {
		return this.playerHandle.equals(player.handle);
	}

	@Override
	public JSONObject asJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("playerHandle", GameLoop.USER.handle);
		jsonObject.put("fromPlanet", fromPlanet);
		jsonObject.put("toPlanet", toPlanet);
		jsonObject.put("fleet", shipsToMove);
		jsonObject.put("duration", duration);
		jsonObject.put("startPosition", startPosition.asJson());
		jsonObject.put("endPosition", endPosition.asJson());
		jsonObject.put("currentPosition", currentPosition.asJson());
		jsonObject.put("executed", "false");

		if (previousPosition != null) {
			jsonObject.put("previousPosition", previousPosition.asJson());

		}

		return jsonObject;
	}

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		fromPlanet = jsonObject.getString("fromPlanet");
		toPlanet = jsonObject.getString("toPlanet");
		shipsToMove = jsonObject.getInt("fleet");
		duration = Float.parseFloat(jsonObject.getString("duration"));
		playerHandle = jsonObject.getString("playerHandle");
		this.currentPosition.consume(jsonObject.getJSONObject("currentPosition"));
		this.previousPosition.consume(jsonObject.getJSONObject("previousPosition"));
		this.startPosition.consume(jsonObject.getJSONObject("startPosition"));
		this.endPosition.consume(jsonObject.getJSONObject("endPosition"));
		startingRound = jsonObject.getInt("startingRound");
		this.executed = jsonObject.getBoolean("executed");

		this.battleStats = new BattleStats();

		battleStats.consume(jsonObject.optJSONObject("battlestats"));

		currentAnimation = previousPosition;

		animation.target(currentPosition.x, currentPosition.y);

	}

	public float angleOfMovement() {
		return new Vector2(endPosition.x - currentAnimation.x, endPosition.y - currentAnimation.y).angle();

	}
	
	public Planet fromPlanet(List<Planet> planets){
		return findPlanetForMove(planets, fromPlanet);
	}
	
	public Planet toPlanet(List<Planet> planets){
		return findPlanetForMove(planets, toPlanet);
	}
	
	private Planet findPlanetForMove(List<Planet> planets, String searchPlanet) {
		
		
		for(Planet planet : planets){
			if(searchPlanet.equals(planet.name)){
				return planet;
			}
		}
		
		return null;
	}

}

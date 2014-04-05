package com.railwaygames.solarsmash.model;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import aurelienribon.tweenengine.Tween;

import com.badlogic.gdx.math.Vector2;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.model.base.JsonConstructable;
import com.railwaygames.solarsmash.model.base.JsonConvertible;
import com.railwaygames.solarsmash.model.tween.MoveTween;

public class Move extends JsonConvertible implements JsonConstructable {

	public String from;
	public String to;
	public int shipsToMove = 0;
	public float duration = 0;
	public String handle;
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
		return this.handle.equals(player.handle);
	}

	@Override
	public JSONObject asJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("handle", GameLoop.USER.handle);
		jsonObject.put("from", from);
		jsonObject.put("to", to);
		jsonObject.put("fleet", shipsToMove);
		jsonObject.put("duration", duration);
		jsonObject.put("startPos", startPosition.asJson());
		jsonObject.put("endPos", endPosition.asJson());
		jsonObject.put("curPos", currentPosition.asJson());
		jsonObject.put("executed", "false");

		if (previousPosition != null) {
			jsonObject.put("prevPos", previousPosition.asJson());

		}

		return jsonObject;
	}

	@Override
	protected void doConsume(JSONObject jsonObject) throws JSONException {
		from = jsonObject.getString("from");
		to = jsonObject.getString("to");
		shipsToMove = jsonObject.getInt("fleet");
		duration = Float.parseFloat(jsonObject.getString("duration"));
		handle = jsonObject.getString("handle");
		this.currentPosition.consume(jsonObject.getJSONObject("curPos"));
		this.previousPosition.consume(jsonObject.getJSONObject("prevPos"));
		this.startPosition.consume(jsonObject.getJSONObject("startPos"));
		this.endPosition.consume(jsonObject.getJSONObject("endPos"));
		startingRound = jsonObject.getInt("startingRound");
		this.executed = jsonObject.getBoolean("executed");

		this.battleStats = new BattleStats();

		battleStats.consume(jsonObject.optJSONObject("bs"));

		currentAnimation = previousPosition;

		animation.target(currentPosition.x, currentPosition.y);
	}

	public float angleOfMovement() {
		return new Vector2(endPosition.x - previousPosition.x, endPosition.y - previousPosition.y).angle();
	}

	public Planet fromPlanet(List<Planet> planets) {
		return findPlanetForMove(planets, from);
	}

	public Planet toPlanet(List<Planet> planets) {
		return findPlanetForMove(planets, to);
	}

	private Planet findPlanetForMove(List<Planet> planets, String searchPlanet) {
		for (Planet planet : planets) {
			if (searchPlanet.equals(planet.name)) {
				return planet;
			}
		}

		return null;
	}
}

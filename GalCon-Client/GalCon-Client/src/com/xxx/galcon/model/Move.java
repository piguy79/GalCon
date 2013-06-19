package com.xxx.galcon.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.base.JsonConstructable;
import com.xxx.galcon.model.base.JsonConvertible;

public class Move extends JsonConvertible implements JsonConstructable {
	public String fromPlanet;
	public String toPlanet;
	public int shipsToMove = 0;
	public float duration = 0;
	public String playerHandle;
	public Position previousPosition = new Position();
	public Position currentPosition = new Position();
	public Position startPosition = new Position();
	public Position endPosition = new Position();
	
	public float animationx;
	public float animationy;

	
	public boolean belongsToPlayer(Player player){
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

		
		if(previousPosition != null){
			jsonObject.put("previousPosition", previousPosition.asJson());

		}
		
		return jsonObject;
	}

	@Override
	public void consume(JSONObject jsonObject) throws JSONException {
		fromPlanet = jsonObject.getString("fromPlanet");
		toPlanet = jsonObject.getString("toPlanet");
		shipsToMove = jsonObject.getInt("fleet");
		duration = jsonObject.getInt("duration");
		playerHandle = jsonObject.getString("playerHandle");
		this.currentPosition.consume(jsonObject.getJSONObject("currentPosition"));
		this.previousPosition.consume(jsonObject.getJSONObject("previousPosition"));
		this.startPosition.consume(jsonObject.getJSONObject("startPosition"));
		this.endPosition.consume(jsonObject.getJSONObject("endPosition"));
		
		animationx = previousPosition.x;
		animationy = previousPosition.y;

		
	}
	
	public void animate(float duration){
		animationx = Interpolation.linear.apply(animationx, currentPosition.x, duration);
		animationy = Interpolation.linear.apply(animationy, currentPosition.y, duration);
	}
	
	public float angleOfMovement(){
		return new Vector2(endPosition.x - currentPosition.x, endPosition.y - currentPosition.y).angle();
		
	}
	
}

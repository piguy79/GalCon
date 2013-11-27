package com.xxx.galcon.model;

import static com.xxx.galcon.Constants.OWNER_NO_ONE;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.xxx.galcon.Constants;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.model.base.JsonConvertible;

public class Planet extends JsonConvertible {
	public String owner = Constants.OWNER_NO_ONE;
	public float shipRegenRate = 1.0f;
	public int numberOfShips;
	public String name;
	public String id;
	public Point position;
	public boolean touched = false;
	public String ability;
	public Harvest harvest;
	public String status;
	private float [] planetBits = new float[4];

	
	public static final String ALIVE = "ALIVE";
	
	private static final int INDEX_PLANET_OWNED_BY_USER = 0;
	private static final int INDEX_PLANET_OWNED_BY_ENEMY = 1;
	private static final int INDEX_PLANET_TOUCHED = 2;
	private static final int INDEX_PLANET_ABILITY = 3;
	
	private static final Color OWNED_BY_ME_COLOR = Color.valueOf("04B404FF");
	private static final Color OWNED_BY_OPPONENT_COLOR = Color.valueOf("FE2E2EFF");
	private static final Color ABILITY_PLANET_COLOR = Color.valueOf("2E9AFEFF");

	
	

	public Planet() {

	}

	@Override
	public void consume(JSONObject jsonObject) {
		try {
			this.name = jsonObject.getString(Constants.NAME);
			this.shipRegenRate = (float) jsonObject.getDouble(Constants.SHIP_REGEN_RATE);
			this.numberOfShips = jsonObject.getInt(Constants.NUMBER_OF_SHIPS);
			this.ability = jsonObject.getString(Constants.ABILITY);
			if (jsonObject.has(Constants.OWNER_HANDLE)) {
				this.owner = jsonObject.getString(Constants.OWNER_HANDLE);
			}
			JSONObject positionJson = jsonObject.getJSONObject(Constants.POSITION);
			Point position = new Point();
			position.consume(positionJson);
			this.position = position;
			this.id = jsonObject.getString(Constants.ID);
			if(jsonObject.has("harvest")){
				this.harvest = new Harvest();
				this.harvest.consume(jsonObject.getJSONObject("harvest"));
			}
			this.status = jsonObject.getString("status");

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public boolean isOwnedBy(Player player) {
		return owner.equals(player.handle);
	}

	public boolean hasAbility() {
		return ability != null && !ability.isEmpty();
	}

	public String getAbilityDescription() {
		return Constants.PLANET_ABILITIES.get(ability);
	}
	
	public List<Move> associatedTargetMoves(GameBoard gameBoard){
		List<Move> associatedMoves = new ArrayList<Move>();
		
		for(Move move : gameBoard.movesInProgress){
			if(move.toPlanet.equals(this.name) && move.belongsToPlayer(GameLoop.USER)){
				associatedMoves.add(move);
			}
		}
		
		return associatedMoves;
	}
	
	public boolean isBeingAttacked(GameBoard gameBoard){
		for(Move move : associatedTargetMoves(gameBoard)){
			if(move.executed && !move.animation.isFinished()){
				return true;
			}
		}
		
		return false;
	}

	public String previousRoundOwner(GameBoard gameBoard) {
		for(Move move : associatedTargetMoves(gameBoard)){
			if(move.executed){
				if(move.battleStats.previousPlanetOwner == null || move.battleStats.previousPlanetOwner.equals("")){
					return Constants.OWNER_NO_ONE;
				}
				return move.battleStats.previousPlanetOwner;
			}
		}
		
		
		return owner;
	}

	public int numberOfShipsToDisplay(GameBoard gameBoard, boolean overrideAnimation) {

		if(overrideAnimation){
			return numberOfShips;
		}
		
		int lowestFromExecutedMoves = 10000000;
		boolean executedMovesFound = false;
		
		if(this.isBeingAttacked(gameBoard)){
			for(Move move : associatedTargetMoves(gameBoard)){
				if(move.executed && move.battleStats.previousShipsOnPlanet < lowestFromExecutedMoves){
					executedMovesFound = true;
					lowestFromExecutedMoves =  move.battleStats.previousShipsOnPlanet;
				}
			}
		}
		
		if(executedMovesFound){
			return lowestFromExecutedMoves;
		}
		return numberOfShips;
	}
	
	public boolean isUnderHarvest(){
		return harvest != null && harvest.isActive();
	}
	
	public boolean isSavedFromHarvest(){
		return harvest != null && !harvest.isActive();
	}
	
	public boolean isAlive(){
		return this.status.equals(ALIVE);
	}
	
	public float[] getPlanetBits() {
		
		String planetOwner = owner;
		planetBits[INDEX_PLANET_TOUCHED] = touched ? 1.0f : 0.0f;
		planetBits[INDEX_PLANET_ABILITY] = hasAbility() ? 1.0f : 0.0f;
		planetBits[INDEX_PLANET_OWNED_BY_USER] = planetOwner.equals(GameLoop.USER.handle) ? 1.0f : 0.0f;
		planetBits[INDEX_PLANET_OWNED_BY_ENEMY] = !planetOwner.equals(OWNER_NO_ONE)
				&& !planetOwner.equals(GameLoop.USER.handle) ? 1.0f : 0.0f;
		
		return planetBits;
	}

	public Color getColor() {
		Color color = Color.valueOf("D8D8D8FF");
		
		
		
		if(isOwnedBy(GameLoop.USER)){
			color = color.mul(OWNED_BY_ME_COLOR);
		}else if(!owner.equals(OWNER_NO_ONE) && !isOwnedBy(GameLoop.USER)){
			color =   color.mul(OWNED_BY_OPPONENT_COLOR);
		}else {
			if(hasAbility()){
				return ABILITY_PLANET_COLOR;
			}
			return color;
		}
		
		if(hasAbility()){
			color = color.add(new Color(0,0,0.5f, 1));
		}
		return color;
	}
	

}

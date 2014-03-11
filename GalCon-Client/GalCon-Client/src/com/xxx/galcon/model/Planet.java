package com.xxx.galcon.model;

import static com.xxx.galcon.Constants.OWNER_NO_ONE;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.graphics.Color;
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
	public float population;
	public boolean isHome;

	public static final String ALIVE = "ALIVE";

	public Planet() {

	}

	@Override
	protected void doConsume(JSONObject jsonObject) {
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
			this.population = jsonObject.getInt(Constants.POPULATION);
			this.id = jsonObject.getString(Constants.ID);
			if (jsonObject.has("harvest")) {
				this.harvest = new Harvest();
				this.harvest.consume(jsonObject.getJSONObject("harvest"));
			}
			this.status = jsonObject.getString("status");
			this.isHome = jsonObject.optString("isHome", "N").equals("Y") ? true : false;

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

	public List<Move> associatedTargetMoves(GameBoard gameBoard) {
		List<Move> associatedMoves = new ArrayList<Move>();

		for (Move move : gameBoard.movesInProgress) {
			if (move.toPlanet.equals(this.name) && move.belongsToPlayer(GameLoop.USER)) {
				associatedMoves.add(move);
			}
		}

		return associatedMoves;
	}

	public boolean isBeingAttacked(GameBoard gameBoard) {
		for (Move move : associatedTargetMoves(gameBoard)) {
			if (move.executed && !move.animation.isFinished()) {
				return true;
			}
		}

		return false;
	}

	public String previousRoundOwner(GameBoard gameBoard) {
		for (Move move : associatedTargetMoves(gameBoard)) {
			if (move.executed) {
				if (move.battleStats.previousPlanetOwner == null || move.battleStats.previousPlanetOwner.equals("")) {
					return Constants.OWNER_NO_ONE;
				}
				return move.battleStats.previousPlanetOwner;
			}
		}

		return owner;
	}

	public int numberOfShipsToDisplay(GameBoard gameBoard, boolean overrideAnimation) {
		if (overrideAnimation) {
			return numberOfShips;
		}

		int lowestFromExecutedMoves = 10000000;
		boolean executedMovesFound = false;

		if (isBeingAttacked(gameBoard)) {
			for (Move move : associatedTargetMoves(gameBoard)) {
				if (move.executed && move.battleStats.previousShipsOnPlanet < lowestFromExecutedMoves) {
					executedMovesFound = true;
					lowestFromExecutedMoves = move.battleStats.previousShipsOnPlanet;
				}
			}
		}

		if (executedMovesFound) {
			return lowestFromExecutedMoves;
		}
		return numberOfShips;
	}

	public boolean isUnderHarvest() {
		return harvest != null && harvest.isActive();
	}

	public boolean isSavedFromHarvest() {
		return harvest != null && !harvest.isActive();
	}

	public boolean isAlive() {
		return this.status.equals(ALIVE);
	}

	public Color getColor() {
		Color OWNED_BY_ME_COLOR = Color.valueOf("2F8705");
		Color OWNED_BY_OPPONENT_COLOR = Color.valueOf("971011");
		Color DEFAULT_PLANET_COLOR = Color.valueOf("595B5C");

		Color color = DEFAULT_PLANET_COLOR;

		if (isOwnedBy(GameLoop.USER)) {
			return OWNED_BY_ME_COLOR;
		} else if (!owner.equals(OWNER_NO_ONE) && !isOwnedBy(GameLoop.USER)) {
			return OWNED_BY_OPPONENT_COLOR;
		} else if (isHome) {
			return OWNED_BY_OPPONENT_COLOR;
		}

		return color;
	}
}

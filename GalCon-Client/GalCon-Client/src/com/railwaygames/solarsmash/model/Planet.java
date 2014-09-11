package com.railwaygames.solarsmash.model;

import static com.railwaygames.solarsmash.Constants.OWNER_NO_ONE;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.config.ConfigConstants;
import com.railwaygames.solarsmash.model.base.JsonConvertible;

public class Planet extends JsonConvertible {
	public String owner = Constants.OWNER_NO_ONE;
	public float regen = 1.0f;
	public int ships;
	public String name;
	public String id;
	public Point position;
	public boolean touched = false;
	public String ability;
	public Harvest harvest;
	public String status;
	public boolean isHome;

	public static final String ALIVE = "ALIVE";

	public Planet() {

	}

	@Override
	protected void doConsume(JSONObject jsonObject) {
		try {
			this.name = jsonObject.getString(Constants.NAME);
			this.regen = (float) jsonObject.getDouble(Constants.SHIP_REGEN_RATE);
			this.ships = jsonObject.getInt(Constants.NUMBER_OF_SHIPS);
			this.ability = jsonObject.optString(Constants.ABILITY, "");
			if (jsonObject.has(Constants.OWNER_HANDLE)) {
				this.owner = jsonObject.getString(Constants.OWNER_HANDLE);
			}
			JSONObject positionJson = jsonObject.getJSONObject(Constants.POSITION);
			Point position = new Point();
			position.consume(positionJson);
			this.position = position;
			this.id = jsonObject.getString(Constants.ID);
			if (jsonObject.has("harvest")) {
				this.harvest = new Harvest();
				this.harvest.consume(jsonObject.getJSONObject("harvest"));
			}
			this.status = jsonObject.optString("status", "ALIVE");
			this.isHome = jsonObject.optString("isHome", "N").equals("Y") ? true : false;
		} catch (JSONException e) {
			Gdx.app.log("PLANET", "Could not parse planet: " + e);
		}
	}

	public boolean isOwnedBy(String handle) {
		return owner.equals(handle);
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
			if (move.to.equals(this.name)) {
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
				if (move.battleStats.previousPlanetOwner == null) {
					continue;
				}
				if (move.battleStats.previousPlanetOwner.equals("")) {
					return Constants.OWNER_NO_ONE;
				}
				return move.battleStats.previousPlanetOwner;
			}
		}

		return owner;
	}

	public int numberOfShipsToDisplay(GameBoard gameBoard, boolean overrideAnimation) {
		if (overrideAnimation) {
			return ships;
		}

		Integer previousShips = null;

		boolean changedOwner = false;
		if (isBeingAttacked(gameBoard)) {
			List<Move> moves = associatedTargetMoves(gameBoard);
			for (Move move : moves) {
				if (!owner.equals(move.battleStats.previousPlanetOwner)) {
					changedOwner = true;
					break;
				}
			}
			for (Move move : moves) {
				if (move.executed && !move.battleStats.diedInAirAttack) {
					if (previousShips == null) {
						previousShips = move.battleStats.previousShipsOnPlanet;
						continue;
					}

					if (changedOwner) {
						if (!owner.equals(move.battleStats.previousPlanetOwner)) {
							previousShips = Math.max(previousShips, move.battleStats.previousShipsOnPlanet);
						}
					} else {
						previousShips = Math.min(previousShips, move.battleStats.previousShipsOnPlanet);
					}
				}
			}
		}

		if (previousShips != null) {
			return previousShips;
		}
		return ships;
	}

	public boolean isUnderHarvest() {
		return harvest != null && harvest.isActive();
	}

	public boolean isSavedFromHarvest(GameBoard gameBoard) {
		return harvest != null && harvest.saveRound == gameBoard.roundInformation.round - 1;
	}

	public int roundsUntilHarvestIsComplete(GameBoard gameBoard) {
		int harvestRounds = Integer.parseInt(gameBoard.gameConfig.getValue(ConfigConstants.HARVEST_ROUNDS));
		return harvest.startingRound + 1 + harvestRounds - gameBoard.roundInformation.round;
	}

	public boolean isAlive() {
		return this.status.equals(ALIVE);
	}

	public Color getColor(String handle, boolean bright) {
		Color OWNED_BY_ME_COLOR = Color.valueOf("2F8705");
		Color OWNED_BY_OPPONENT_COLOR = Color.valueOf("971011");
		Color DEFAULT_PLANET_COLOR = Color.valueOf("595B5C");

		if (bright) {
			OWNED_BY_ME_COLOR = Constants.Colors.USER_SHIP_FILL;
			OWNED_BY_OPPONENT_COLOR = Constants.Colors.ENEMY_SHIP_FILL;
			DEFAULT_PLANET_COLOR = Constants.Colors.NEUTRAL;
		}

		Color color = DEFAULT_PLANET_COLOR;

		if (!owner.equals(OWNER_NO_ONE) && handle.equals(GameLoop.getUser().handle)) {
			return OWNED_BY_ME_COLOR;
		} else if (!owner.equals(OWNER_NO_ONE) && !handle.equals(GameLoop.getUser().handle)) {
			return OWNED_BY_OPPONENT_COLOR;
		} else if (isHome) {
			return OWNED_BY_OPPONENT_COLOR;
		}

		return color;
	}

	public boolean isOwned() {
		return isHome || !owner.equals(OWNER_NO_ONE);
	}
}

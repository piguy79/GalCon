package com.xxx.galcon.model.factory;

import java.util.List;

import com.xxx.galcon.GameLoop;
import com.xxx.galcon.math.GalConMath;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;

public class MoveFactory {

	public static Move createMove(List<Planet> availablePlanets, int fleetToSend, int round) {
		if (fleetToSend <= 0) {
			return null;
		}

		Move move = new Move();

		float startX = -1, startY = -1, endX = 1, endY = -1;
		for (Planet planet : availablePlanets) {
			if (planet.isOwnedBy(GameLoop.USER) && move.fromPlanet == null) {
				move.fromPlanet = planet.name;
				move.shipsToMove = fleetToSend;
				move.previousPosition = planet.position;
				move.currentPosition = planet.position;
				move.startPosition = planet.position;
				startX = planet.position.x;
				startY = planet.position.y;
				move.startingRound = round;
			} else {
				move.toPlanet = planet.name;
				move.endPosition = planet.position;
				endX = planet.position.x;
				endY = planet.position.y;
			}
		}

		if (startX == -1 || startY == -1) {
			throw new RuntimeException("Could not find start planet for move creation");
		} else if (endX == -1 || endY == -1) {
			throw new RuntimeException("Could not find end planet for move creation");
		} else {
			move.playerHandle = GameLoop.USER.handle;
			move.duration = GalConMath.distance(startX, startY, endX, endY);

			return move;
		}
	}
}

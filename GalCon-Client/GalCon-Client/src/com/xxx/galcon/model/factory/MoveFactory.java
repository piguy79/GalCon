package com.xxx.galcon.model.factory;

import java.util.List;

import com.xxx.galcon.GameLoop;
import com.xxx.galcon.math.GalConMath;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;

public class MoveFactory {

	public Move createMove(List<Planet> availablePlanets, int fleetToSend){
		Move move = new Move();

		float startX = 0, startY = 0, endX = 0, endY = 0;
		for (Planet planet : availablePlanets) {
			if (planet.isOwnedBy(GameLoop.USER) && move.fromPlanet == null) {
				move.fromPlanet = planet.name;
				move.shipsToMove = fleetToSend;
				move.previousPosition = planet.position;
				move.currentPosition = planet.position;
				move.startPosition = planet.position;
				planet.numberOfShips -= move.shipsToMove;
				startX = planet.position.x;
				startY = planet.position.y;
			} else {
				move.toPlanet = planet.name;
				move.endPosition = planet.position;
				endX = planet.position.x;
				endY = planet.position.y;
			} 
		}

		move.duration = GalConMath.distance(startX, startY, endX, endY);
		
		return move;
	}
	
}

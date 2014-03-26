package com.xxx.galcon.model.factory;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.math.GalConMath;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.screen.BoardScreen.BoardCalculations;
import com.xxx.galcon.screen.Resources;

public class MoveFactory {

	public static Move createMove(List<Planet> availablePlanets, int fleetToSend, int round) {
		if (fleetToSend < 0) {
			return null;
		}

		Move move = new Move();

		float startX = -1, startY = -1, endX = 1, endY = -1;
		for (Planet planet : availablePlanets) {
			if (planet.isOwnedBy(GameLoop.USER.handle) && move.fromPlanet == null) {
				move.fromPlanet = planet.name;
				move.shipsToMove = fleetToSend;
				startX = planet.position.x;
				startY = planet.position.y;
				move.startingRound = round;
			} else {
				move.toPlanet = planet.name;
				endX = planet.position.x;
				endY = planet.position.y;
			}
		}

		if (startX == -1 || startY == -1) {
			throw new RuntimeException("Could not find start planet for move creation");
		} else if (endX == -1 || endY == -1) {
			throw new RuntimeException("Could not find end planet for move creation");
		} else {
			float xLength = endX - startX;
			float yLength = endY - startY;

			float dist = (float) sqrt(pow(xLength, 2) + pow(yLength, 2));

			float offsetRadius = 0.08f;
			float ratio = offsetRadius / dist;

			float xOffset = xLength * ratio;
			float yOffset = yLength * ratio;

			startX += xOffset;
			startY += yOffset;

			endX -= xOffset;
			endY -= yOffset;

			move.endPosition = new Point(endX, endY);

			Point pos = new Point(startX, startY);
			move.previousPosition = pos;
			move.currentPosition = pos;
			move.startPosition = pos;
			move.duration = GalConMath.distance(startX, startY, endX, endY);
			move.playerHandle = GameLoop.USER.handle;

			return move;
		}
	}

	public static Image createShipForDisplay(float angle, Point point, Resources resources, BoardCalculations boardCalcs) {
		Image ship = new Image(resources.skin, "shipImage");

		ship.setScaling(Scaling.fillY);
		float targetHeight = boardCalcs.getTileSize().height * 0.33f;
		Vector2 vec = Scaling.fillY.apply(ship.getWidth(), ship.getHeight(), 1, targetHeight);
		ship.setSize(vec.x, vec.y);

		ship.setOrigin(ship.getWidth() / 2, ship.getHeight() / 2);

		Point pos = MoveFactory.getShipPosition(ship, point, boardCalcs);
		ship.setPosition(pos.x, pos.y);

		ship.setRotation(angle);

		return ship;
	}

	public static Point getShipPosition(Image ship, Point point, BoardCalculations boardCalcs) {
		Point pixelPoint = boardCalcs.tileCoordsToPixels(point);
		boardCalcs.centerPoint(pixelPoint, ship);

		return pixelPoint;
	}

	public static HarvestMove createHarvestMove(Planet planet) {
		return new HarvestMove(planet.name);
	}
}

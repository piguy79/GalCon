package com.railwaygames.solarsmash.model.factory;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.math.GalConMath;
import com.railwaygames.solarsmash.model.HarvestMove;
import com.railwaygames.solarsmash.model.Move;
import com.railwaygames.solarsmash.model.Planet;
import com.railwaygames.solarsmash.model.Point;
import com.railwaygames.solarsmash.screen.BoardScreen.BoardCalculations;
import com.railwaygames.solarsmash.screen.Resources;

public class MoveFactory {

	public static Move createMove(List<Planet> availablePlanets, int fleetToSend, int round) {
		if (fleetToSend < 0) {
			return null;
		}

		Move move = new Move();

		float startX = -1, startY = -1, endX = 1, endY = -1;
		for (Planet planet : availablePlanets) {
			if (planet.isOwnedBy(GameLoop.USER.handle) && move.from == null) {
				move.from = planet.name;
				move.shipsToMove = fleetToSend;
				startX = planet.position.x;
				startY = planet.position.y;
				move.startingRound = round;
			} else {
				move.to = planet.name;
				endX = planet.position.x;
				endY = planet.position.y;
			}
		}

		if (startX == -1 || startY == -1) {
			throw new RuntimeException("Could not find start planet for move creation");
		} else if (endX == -1 || endY == -1) {
			throw new RuntimeException("Could not find end planet for move creation");
		} else {
			Point pos = new Point(startX, startY);
			move.previousPosition = pos;
			move.currentPosition = pos;
			move.duration = GalConMath.distance(startX, startY, endX, endY);
			move.handle = GameLoop.USER.handle;

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

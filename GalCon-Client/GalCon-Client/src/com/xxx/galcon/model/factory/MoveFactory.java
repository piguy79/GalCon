package com.xxx.galcon.model.factory;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.math.GalConMath;
import com.xxx.galcon.model.HarvestMove;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;

public class MoveFactory {
	
	private static UISkin skin;

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
	
	public static void setSkin(UISkin skin) {
		MoveFactory.skin = skin;
	}
	
	public static Table createShipForDisplay(Move move, float tileHeight, float tileWidth, Point initialPointInWorld){
		ImageButton shipMoveButton = new ImageButton(skin.get("shipButton", ImageButtonStyle.class));
		shipMoveButton.setHeight(tileHeight * 0.4f);
		shipMoveButton.setWidth(tileWidth * 0.4f);

		shipMoveButton.setOrigin(shipMoveButton.getWidth()/2, shipMoveButton.getHeight()/2);
				
		Table wrapper = setupRotationWrapper(shipMoveButton, tileHeight,
				tileWidth, initialPointInWorld);
		
		wrapper.setRotation(move.angleOfMovement());
		
		return wrapper;
					
	}
	
	private static Table setupRotationWrapper(ImageButton shipMoveButton,
			float tileHeight, float tileWidth, Point movePosition) {
		Table wrapper = new Table();
		
		wrapper.defaults().width(tileWidth * 0.25f).height(tileWidth * 0.25f).pad(0);
		wrapper.add(shipMoveButton);			
		wrapper.setX(movePosition.x + (tileWidth / 2));
		wrapper.setY(movePosition.y + (tileHeight / 2));
		wrapper.setTransform(true);
		wrapper.setOrigin(wrapper.getPrefWidth() / 2, wrapper.getPrefHeight() / 2);
		wrapper.setScaleX(1.5f);
		return wrapper;
	}
	
	public static HarvestMove createHarvestMove(Planet planet){
		return new HarvestMove(planet.name);
	}
}

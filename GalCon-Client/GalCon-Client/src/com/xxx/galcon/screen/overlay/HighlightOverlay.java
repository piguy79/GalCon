package com.xxx.galcon.screen.overlay;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.screen.Resources;

public class HighlightOverlay extends Overlay {

	private List<Planet> planets = new ArrayList<Planet>();
	private List<Move> moves = new ArrayList<Move>();

	public HighlightOverlay(Resources resources, float hudHeight) {
		super(resources);

		super.setBounds(0, hudHeight, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - 2 * hudHeight);
	}

	public void addMove(Move move) {

	}

	public void addPlanet(Planet planet) {

	}
}

package com.xxx.galcon.model;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class Bounds {
	public Size size;
	public Point origin;

	public Bounds(Point origin, Size size) {
		this.origin = origin;
		this.size = size;
	}

	public int getTopY() {
		return (int) origin.y + size.height;
	}

	public void applyBounds(Actor actor) {
		actor.setBounds(origin.x, origin.y, size.width, size.height);
	}
}

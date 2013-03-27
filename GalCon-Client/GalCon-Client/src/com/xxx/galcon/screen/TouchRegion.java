package com.xxx.galcon.screen;

public class TouchRegion {
	private float x, y, width, height;

	public TouchRegion(float x, float y, float width, float height) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean contains(float testX, float testY) {
		if (testX >= x && testX <= x + width) {
			if (testY <= y && testY >= y - height) {
				return true;
			}
		}
		return false;
	}
}

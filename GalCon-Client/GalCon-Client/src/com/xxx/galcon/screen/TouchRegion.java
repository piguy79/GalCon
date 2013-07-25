package com.xxx.galcon.screen;

public class TouchRegion {
	protected int x, y, width, height;
	private boolean font;

	public TouchRegion(int x, int y, int width, int height, boolean font) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.font = font;
	}

	public boolean contains(float testX, float testY) {
		if (testX >= x && testX <= x + width) {
			if (font && testY <= y && testY >= y - height) {
				return true;
			} else if (!font && testY >= y && testY <= y + height) {
				return true;
			}
		}
		return false;
	}
}

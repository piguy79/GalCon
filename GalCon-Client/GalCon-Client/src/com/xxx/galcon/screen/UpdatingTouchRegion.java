package com.xxx.galcon.screen;

public abstract class UpdatingTouchRegion extends TouchRegion {
	
	protected float startingHeight;
	protected float startingWidth;

	public UpdatingTouchRegion(float x, float y, float width, float height,float startingWidth,float startingHeight,
			boolean font) {
		super(x, y, width, height, font);
		this.startingWidth = startingWidth;
		this.startingHeight = startingHeight;
	}
	
	public void updatePoint(float x, float y){
		updateToPoint(x, y);
	}
	
	protected abstract void updateToPoint(float x, float y);

}

package com.xxx.galcon.screen.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.xxx.galcon.InGameInputProcessor;
import com.xxx.galcon.InGameInputProcessor.TouchPoint;

public abstract class DragButton extends Button {
	protected int minX;
	protected int maxX;

	public DragButton(AtlasRegion region) {
		super(region);
	}

	abstract public void updateDragBounds(int x, int y, int width, int height);

	public void setX(int x) {
		x = Math.min(x, maxX);
		this.x = Math.max(x, minX);
	}

	private boolean dragInProgress = false;

	@Override
	public void render(SpriteBatch spriteBatch) {
		super.render(spriteBatch);

		drag();
	}

	protected void drag() {
		InGameInputProcessor ip = (InGameInputProcessor) Gdx.input.getInputProcessor();
		if (ip.isDragging()) {
			if (!dragInProgress) {
				TouchPoint dragBegin = ip.getDragBeginPoint();

				if (isTouched(dragBegin.x, dragBegin.y)) {
					dragInProgress = true;
				}
			} else {
				int offset = ip.getDragXDiff(true);
				this.setX(x + offset);
			}
		} else {
			dragInProgress = false;
		}
	}

	public boolean isDragging() {
		return dragInProgress;
	}

	public float getDragRatio() {
		return (float) (x - minX) / (float) (maxX - minX);
	}
}

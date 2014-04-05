package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Line extends Actor {
	
	private TextureRegion region;
	
	public Line(Color color, float width, TextureRegion region){	
		setColor(color);
		setWidth(width);
		this.region = region;
	}
	
	public void draw (SpriteBatch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}

}

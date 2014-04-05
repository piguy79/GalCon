package com.railwaygames.solarsmash.model.tween;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.Color;

public class ColorTween implements TweenAccessor<Color> {

	public static final int ALPHA = 100235;

	@Override
	public int getValues(Color target, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case ALPHA:
			returnValues[0] = target.r;
			returnValues[1] = target.g;
			returnValues[2] = target.b;
			returnValues[3] = target.a;
			return 4;
		default:
			assert false;
			return -1;
		}
	}

	@Override
	public void setValues(Color target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case ALPHA:
			target.r = newValues[0];
			target.g = newValues[1];
			target.b = newValues[2];
			target.a = newValues[3];
			break;
		default:
			assert false;
			break;
		}
	}
}

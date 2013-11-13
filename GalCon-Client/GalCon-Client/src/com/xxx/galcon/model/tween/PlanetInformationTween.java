package com.xxx.galcon.model.tween;

import aurelienribon.tweenengine.TweenAccessor;

import com.xxx.galcon.screen.PlanetInformationDialog;
import com.xxx.galcon.screen.ShipSelectionDialog;

public class PlanetInformationTween implements TweenAccessor<PlanetInformationDialog> {
	
	public static final int POSITION_XY = 1;

	@Override
	public int getValues(PlanetInformationDialog target, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case POSITION_XY:
			returnValues[0] = target.getX();
			returnValues[1] = target.getY();
			return 2;
		default:
			assert false;
			return -1;
		}
	}

	@Override
	public void setValues(PlanetInformationDialog target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case POSITION_XY:
			target.setX((int) newValues[0]);
			target.setY((int) newValues[1]);
			break;
		default:
			assert false;
			break;
		}
	}

}

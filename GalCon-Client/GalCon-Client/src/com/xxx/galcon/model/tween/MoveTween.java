package com.xxx.galcon.model.tween;

import com.xxx.galcon.model.Move;

import aurelienribon.tweenengine.TweenAccessor;

public class MoveTween implements TweenAccessor<Move> {
	
	public static final int POSITION_XY = 1;

	@Override
    public int getValues(Move target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case POSITION_XY:
                returnValues[0] = target.currentAnimation.x;
                returnValues[1] = target.currentAnimation.y;
                return 2;
            default: assert false; return -1;
        }
    }
    
    @Override
    public void setValues(Move target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case POSITION_XY:
                target.currentAnimation.x = newValues[0];
                target.currentAnimation.y = newValues[1];
                break;
            default: assert false; break;
        }
    }

}

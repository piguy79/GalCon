package com.railwaygames.solarsmash.math;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import com.badlogic.gdx.math.MathUtils;
import com.railwaygames.solarsmash.model.Point;

public class GalConMath {

	public static float distance(float startX, float startY, float endX, float endY) {
		return (float) sqrt(pow(startX - endX, 2.0) + pow(startY - endY, 2.0));
	}

	public static float distance(Point startingPoint, Point endingPoint) {
		return distance(startingPoint.x, startingPoint.y, endingPoint.x, endingPoint.y);
	}

	public static Point nextPointInEllipse(Point center, float xRadius, float yRadius, float currentAngle) {
		float x = (float) (center.x + xRadius * MathUtils.cosDeg(currentAngle));
		float y = (float) (center.y + yRadius * MathUtils.sinDeg(currentAngle));

		return new Point(x, y);
	}
}

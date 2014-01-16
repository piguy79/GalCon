package com.xxx.galcon.math;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import com.xxx.galcon.model.Point;

public class GalConMath {

	public static float distance(float startX, float startY, float endX, float endY) {
		return (float) sqrt(pow(startX - endX, 2.0) + pow(startY - endY, 2.0));
	}
	
	public static float distance(Point startingPoint, Point endingPoint) {
		return distance(startingPoint.x, startingPoint.y, endingPoint.x, endingPoint.y);
	}
	
	public static Point nextPointInEllipse(Point center, float xRadius, float yRadius, float currentAngle){
		 double radian = degreeToRadian(currentAngle);
		 
		 float x = (float) (center.x + xRadius * Math.cos(radian));
		 float y = (float) (center.y + yRadius * Math.sin(radian));
		 
		 return new Point(x, y);
	}
	
	private static double degreeToRadian(float degree){
		return degree * (Math.PI / 180);
	}
	
}

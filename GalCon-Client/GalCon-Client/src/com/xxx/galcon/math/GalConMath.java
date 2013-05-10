package com.xxx.galcon.math;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class GalConMath {

	public static float distance(int startX, int startY, int endX, int endY) {
		return (float) sqrt(pow(startX - endX, 2.0) + pow(startY - endY, 2.0));
	}
}

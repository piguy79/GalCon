package com.xxx.galcon.math;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.xxx.galcon.screen.BoardScreen.WorldPlane;

public class WorldMath {

	public static Vector2 screenXYToWorldXY(Camera camera, float screenX, float screenY) {
		Vector3 worldTouchNear = new Vector3(screenX, screenY, 0);
		camera.unproject(worldTouchNear);

		Vector3 worldTouchFar = new Vector3(screenX, screenY, 1);
		camera.unproject(worldTouchFar);

		float worldPercent = (WorldPlane.Z - worldTouchNear.z) / (worldTouchFar.z - worldTouchNear.z);

		float worldX = worldPercent * (worldTouchFar.x - worldTouchNear.x);
		float worldY = worldPercent * (worldTouchFar.y - worldTouchNear.y);

		return new Vector2(worldX, worldY);
	}
}

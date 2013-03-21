package com.xxx.galcon;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;

public interface Screen {
	/**
	 * While this screen is active, it will have it's render method called for
	 * every frame.
	 * 
	 * @return If this screen should not be shown again, return false;
	 */
	public boolean render(GL20 gl, Camera camera);
	
	public Object getRenderReturnValue();
	
	public void dispose();
}

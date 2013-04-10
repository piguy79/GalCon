package com.xxx.galcon;

import com.badlogic.gdx.Screen;

public interface ScreenFeedback extends Screen {

	/**
	 * @return If the screen has decided to termimate, it will return a non-null
	 *         object that can drive the next screen to show.
	 */
	public Object getRenderResult();

	/**
	 * Screens can be reused rather than disposed. Before being reused,
	 * resetState will be called to clean up any old stuff.
	 */
	public void resetState();
}

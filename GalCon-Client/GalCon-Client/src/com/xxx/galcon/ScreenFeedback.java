package com.xxx.galcon;

import com.badlogic.gdx.Screen;

public interface ScreenFeedback extends Screen {

	/**
	 * @return If the screen has decided to termimate, it will return a non-null
	 *         object that can drive the next screen to show.
	 */
	public Object getRenderResult();
}

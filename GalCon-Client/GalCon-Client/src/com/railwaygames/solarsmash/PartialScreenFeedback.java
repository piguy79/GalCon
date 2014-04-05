package com.railwaygames.solarsmash;

import com.badlogic.gdx.scenes.scene2d.Stage;

public interface PartialScreenFeedback {

	public void show(Stage stage, float width, float height);

	public void hide();

	public void render(float delta);

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
	
	public boolean hideTitleArea();
}

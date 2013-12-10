package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.xxx.galcon.Constants;

public class WaitImageButton extends ImageButton {

	private long startTime;

	public WaitImageButton(Skin skin) {
		super(skin, Constants.UI.WAIT_BUTTON);

		setVisible(false);
	}

	public void start() {
		startTime = System.currentTimeMillis();
		setVisible(true);
	}

	public void stop() {
		startTime = 0;
		setVisible(false);
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		float elapsedSeconds = (float) (System.currentTimeMillis() - startTime) / 1000.0f;

		float alpha = elapsedSeconds % 2;
		if (alpha >= 1) {
			alpha = 2 - alpha;
		}

		super.draw(batch, alpha);
	}
}

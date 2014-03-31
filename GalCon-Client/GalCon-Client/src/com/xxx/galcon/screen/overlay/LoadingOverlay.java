package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class LoadingOverlay extends Overlay {

	public LoadingOverlay(Resources resources) {
		super(resources);
		WaitImageButton waitImage = new WaitImageButton(resources.skin);
		float buttonWidth = .25f * (float) Gdx.graphics.getWidth();
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(Gdx.graphics.getWidth() / 2 - buttonWidth / 2);
		waitImage.setY(Gdx.graphics.getHeight() / 2 - buttonWidth / 2);
		addActor(waitImage);
		
		waitImage.start();
	}
	

}

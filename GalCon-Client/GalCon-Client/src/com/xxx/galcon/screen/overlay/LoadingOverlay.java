package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class LoadingOverlay extends Overlay {

	public LoadingOverlay(Resources resources) {
		super(resources);
		WaitImageButton waitImage = new WaitImageButton(resources.skin);
		waitImage.setWidth(Gdx.graphics.getWidth() * 0.3f);
		waitImage.setHeight(Gdx.graphics.getWidth() * 0.3f);
		waitImage.setX(Gdx.graphics.getWidth() / 2 - waitImage.getWidth() / 2);
		waitImage.setY(Gdx.graphics.getHeight() / 2 - (waitImage.getHeight() / 2));
		addActor(waitImage);
		
		waitImage.start();
	}
	

}

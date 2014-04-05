package com.railwaygames.solarsmash.screen.overlay;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.screen.Resources;

public class DialogOverlay extends Overlay {

	public DialogOverlay(Resources resources) {
		super(resources);
		this.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
			}
		});
	}

}

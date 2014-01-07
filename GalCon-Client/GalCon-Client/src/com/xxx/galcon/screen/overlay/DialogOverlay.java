package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class DialogOverlay extends Overlay{
	
	public DialogOverlay(TextureAtlas atlas){
		super(atlas);
		this.addListener(new ClickListener(){@Override
		public void clicked(InputEvent event, float x, float y) {
		}});
	}

}

package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.xxx.galcon.Function;

public class DismissableOverlay extends Overlay {

	private Overlay delegate;

	public DismissableOverlay(TextureAtlas menusAtlas, Overlay delegate, ClickListener clickListener) {
		super(menusAtlas);
		this.delegate = delegate;

		this.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				DismissableOverlay.this.remove();
			}
		});
		this.addListener(clickListener);
	}
	
	public DismissableOverlay(TextureAtlas menusAtlas,final Function func){
		super(menusAtlas);
		
		this.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				DismissableOverlay.this.remove();
				func.apply();
			}
		});
	}

}

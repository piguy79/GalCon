package com.xxx.galcon.screen.overlay;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

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
		if (clickListener != null) {
			this.addListener(clickListener);
		}
	}

	@Override
	protected void doCustomRender(SpriteBatch batch) {
		delegate.doCustomRender(batch);
	}
}
